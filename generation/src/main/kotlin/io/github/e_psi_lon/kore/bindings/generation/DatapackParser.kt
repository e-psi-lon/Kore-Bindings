package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.DatapackBuilder
import io.github.e_psi_lon.kore.bindings.generation.data.Macro
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import io.github.e_psi_lon.kore.bindings.generation.data.Scoreboard
import io.github.e_psi_lon.kore.bindings.generation.data.Storage
import kotlinx.coroutines.*
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension

class DatapackParser(
    private val datapackDir: Path,
    private val logger: Logger
) {

    private val datapackBuilder = DatapackBuilder()

    /**
     * Parses the datapack directory and generates bindings for all functions.
     * Uses coroutines to parse namespaces in parallel for better performance.
     */
    operator fun invoke(): Datapack = runBlocking {
        logger.info("Parsing datapack at $datapackDir")
        val namespaces = datapackDir.resolve("data").toFile().listFiles { file -> file.isDirectory }!!

        val parsedNamespaces = namespaces.map { namespace ->
            async(Dispatchers.Default) {
                val namespaceName = namespace.name
                val prefix = if (namespaceName.contains('.')) namespaceName.substringBefore('.') else null
                logger.debug("Processing namespace $namespaceName with prefix $prefix")
                handleNamespace(namespaceName, prefix)
            }
        }.awaitAll()

        parsedNamespaces.forEach { datapackBuilder.addNamespace(it) }
        datapackBuilder.build()
    }


    private suspend fun handleNamespace(namespace: String, prefix: String?): ParsedNamespace {
        val namespaceDir = datapackDir.resolve(namespace)
        val components = mutableListOf<Component>()
        val storages: MutableSet<Storage> = mutableSetOf()
        val scoreboards: MutableSet<Scoreboard> = mutableSetOf()
        val macros = mutableListOf<Macro>()
        DatapackComponentType.values().forEach { type ->
            val directory = namespaceDir.resolve(type.directoryName)
            if (directory.exists()) {
                logger.debug("Processing type $type in namespace $namespace")
                when (type) {
                    DatapackComponentType.FUNCTION -> {
                        val (
                            functions,
                            namespaceStorages,
                            namespaceScoreboards
                        ) = handleFunction(directory, namespace)
                        components.addAll(functions)
                        storages.addAll(namespaceStorages)
                        scoreboards.addAll(namespaceScoreboards)
                        macros.addAll(functions.mapNotNull { it.macro }.distinct())
                    }
                    DatapackComponentType.FUNCTION_TAG -> handleFunctionTagComponents(directory).let { components.addAll(it) }
                    else -> handleRegularComponents(directory, type).let { components.addAll(it) }
                }
            }
        }
        return ParsedNamespace(
            namespace,
            prefix,
            components.groupBy { it.componentType },
            storages,
            scoreboards,
            macros
        )
    }

    private fun <T : Component> handleComponents(
        path: Path,
        fileExtension: String,
        componentBuilder: (relativePath: Path, name: String) -> T
    ): List<T> {
        return path.toFile().walkTopDown()
            .filter { it.isFile && it.extension == fileExtension }
            .map {
                val relativePath = path.relativize(it.toPath()).toString().replace("\\", "/")
                val name = relativePath.removeSuffix(".$fileExtension")
                componentBuilder(path.relativize(it.toPath()), name)
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

    private suspend fun handleFunction(path: Path, namespace: String): Triple<List<Component.Function>, Set<Storage>, Set<Scoreboard>> = coroutineScope {
        val files = path.toFile().walkTopDown()
            .filter { it.isFile && it.extension == "mcfunction" }
            .toList()

        // Step 1: Read all files in parallel (I/O-bound)
        val fileContentJobs = mutableListOf<Deferred<Pair<Path, String>>>()
        for (file in files) {
            fileContentJobs.add(async(Dispatchers.IO) {
                val relativePath = path.relativize(file.toPath())
                relativePath to file.readText()
            })
        }
        val fileContents = fileContentJobs.awaitAll()

        // Step 2: Parse all file contents in parallel (CPU-bound: regex, string operations)
        val parseJobs = mutableListOf<Deferred<Triple<Component.Function, Set<Storage>, Set<Scoreboard>>>>()
        for ((relativePath, content) in fileContents) {
            parseJobs.add(async(Dispatchers.Default) {
                val parser = FunctionParser2(
                    content,
                    namespace,
                    relativePath,
                    logger
                )
                val (scoreboardsInFunction, storagesInFunction, macro) = parser()

                Triple(
                    Component.Function(
                        relativePath,
                        relativePath.nameWithoutExtension,
                        macro
                    ),
                    storagesInFunction,
                    scoreboardsInFunction
                )
            })
        }
        val results = parseJobs.awaitAll()

        val functions = results.map { it.first }
        val storages = results.flatMap { it.second }.toSet()
        val scoreboards = results.flatMap { it.third }.toSet()

        Triple(functions, storages, scoreboards)
    }
}
