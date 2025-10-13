package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.DatapackBuilder
import io.github.e_psi_lon.kore.bindings.generation.data.Macro
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import io.github.e_psi_lon.kore.bindings.generation.data.Scoreboard
import io.github.e_psi_lon.kore.bindings.generation.data.Storage
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension

class DatapackParser(
    private val datapackDir: Path,
    private val logger: Logger
) {

    private val datapackBuilder = DatapackBuilder()

    /**
     * Parses the datapack directory and generates bindings for all functions.
     */
    operator fun invoke(): Datapack {
        logger.info("Parsing datapack at $datapackDir")
        val namespaces = datapackDir.resolve("data").toFile().listFiles { file -> file.isDirectory }!!
        namespaces.forEach { namespace ->
            val namespaceName = namespace.name
            val prefix = if (namespaceName.contains('.')) namespaceName.substringBefore('.') else null
            logger.debug("Processing namespace $namespaceName with prefix $prefix")
            datapackBuilder.addNamespace(handleNamespace(namespaceName, prefix))
        }
        return datapackBuilder.build()
    }


    private fun handleNamespace(namespace: String, prefix: String?): ParsedNamespace {
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


    private fun handleFunction(path: Path, namespace: String): Triple<List<Component.Function>, Set<Storage>, Set<Scoreboard>> {
        val storages = mutableSetOf<Storage>()
        val scoreboards = mutableSetOf<Scoreboard>()
        val functions = mutableListOf<Component.Function>()

        path.toFile().walkTopDown()
            .filter { it.isFile && it.extension == "mcfunction" }
            .forEach { file ->
                // Get relative path from the function directory
                val relativePath = path.relativize(file.toPath())

                val parse = FunctionParser2(
                    file.readText(),
                    namespace,
                    relativePath,
                    logger
                )

                val (scoreboardsInFunction, storagesInFunction, macro) = parse()

                functions.add(
                    Component.Function(
                        relativePath,
                        relativePath.nameWithoutExtension,
                        macro
                    )
                )
                storages.addAll(storagesInFunction)
                scoreboards.addAll(scoreboardsInFunction)
            }

        return Triple(functions, storages, scoreboards)
    }
}
