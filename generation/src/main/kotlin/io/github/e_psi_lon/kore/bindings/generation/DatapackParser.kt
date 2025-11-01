package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.*

class DatapackParser(
    private val datapackDir: Path,
    private val logger: Logger
) {

    private val dataDir = datapackDir.resolve("data")
    private val datapackBuilder = DatapackBuilder()

    /**
     * Parses the datapack directory and generates bindings for all functions.
     * Uses coroutines to parse namespaces in parallel for better performance.
     */
    operator fun invoke(): Datapack = runBlocking {
        logger.info("Parsing datapack at $datapackDir")
        val namespaces = dataDir
            .listDirectoryEntries()
            .filter { file -> file.isDirectory() }
        val parsedNamespaces = namespaces.mapAsync(Dispatchers.Default) { namespace ->
            val namespaceName = namespace.fileName.toString()
            val prefix = if (namespaceName.contains('.')) namespaceName.substringBefore('.') else null
            logger.debug("Processing namespace $namespaceName with prefix $prefix")
            handleNamespace(namespaceName, prefix)
        }

        parsedNamespaces.forEach { datapackBuilder.addNamespace(it) }
        datapackBuilder.build()
    }


    private suspend fun handleNamespace(namespace: String, prefix: String?): ParsedNamespace {
        val namespaceDir = dataDir.resolve(namespace)
        val components = mutableListOf<Component>()
        val storages: MutableSet<Storage> = mutableSetOf()
        val scoreboards: MutableSet<Scoreboard> = mutableSetOf()
        val macros = mutableListOf<Macro>()
        val results = DatapackComponentType.entries.toTypedArray().mapAsync(Dispatchers.Default) { type ->
            val directory = namespaceDir.resolve(type.directoryName)
            if (directory.exists()) {
                logger.debug("Processing type $type in namespace $namespace")
                when (type) {
                    DatapackComponentType.FUNCTION -> handleFunction(directory, namespace)
                    DatapackComponentType.FUNCTION_TAG -> Triple(
                        handleFunctionTagComponents(directory),
                        emptySet(),
                        emptySet()
                    )
                    else -> Triple(
                        handleRegularComponents(directory, type),
                        emptySet(),
                        emptySet()
                    )
                }
            } else {
                Triple(emptyList(), emptySet(), emptySet())
            }
        }
        results.forEach { (resultComponents , resultStorages, resultScoreboards) ->
            components.addAll(resultComponents)
            storages.addAll(resultStorages)
            scoreboards.addAll(resultScoreboards)
            if (resultComponents.isNotEmpty() && resultComponents.first() is Component.Function) {
                macros.addAll(resultComponents.mapNotNull { (it as? Component.Function)?.macro }.distinct())
            }
        }
        return ParsedNamespace(
            namespace,
            prefix,
            components,
            storages,
            scoreboards,
            macros
        )
    }

    @OptIn(ExperimentalPathApi::class)
    private fun <T : Component> handleComponents(
        path: Path,
        fileExtension: String,
        componentBuilder: (relativePath: Path, name: String) -> T
    ): List<T> {
        return path.walk()
            .filter { it.isRegularFile() && it.extension == fileExtension }
            .map {
                val relativePath = path.relativize(it)
                val name = relativePath.nameWithoutExtension
                componentBuilder(relativePath, name)
            }.toList()
    }

    private fun handleRegularComponents(path: Path, componentType: DatapackComponentType): List<Component> =
        handleComponents(path, componentType.fileExtension) { relPath, name ->
            Component.Simple(relPath, name, componentType)
        }

    private fun handleFunctionTagComponents(path: Path): List<Component.FunctionTag> =
        handleComponents(path, "json") { relPath, name ->
            Component.FunctionTag(relPath, name)
        }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun handleFunction(path: Path, namespace: String): Triple<List<Component.Function>, Set<Storage>, Set<Scoreboard>> {
        val files = path.walk().filter { it.isRegularFile() && it.extension == "mcfunction" }.toList()

        // Step 1: Read all files in parallel (I/O-bound)
        val fileContents = files.mapAsync(Dispatchers.IO) { file ->
            val relativePath = path.relativize(file)
            relativePath to file.readText()
        }

        // Step 2: Parse all file contents in parallel (CPU-bound: regex, string operations)
        val results = fileContents.mapAsync(Dispatchers.Default) { (relativePath, content) ->
            val parser = FunctionParser2(content, namespace, relativePath, logger)
            val (scoreboardsInFunction, storagesInFunction, macro) = parser()
            Triple(
                Component.Function(relativePath, relativePath.nameWithoutExtension, macro),
                storagesInFunction,
                scoreboardsInFunction
            )
        }

        val functions = results.map { it.first }
        val storages = results.flatMap { it.second }.toSet()
        val scoreboards = results.flatMap { it.third }.toSet()

        return Triple(functions, storages, scoreboards)
    }
}
