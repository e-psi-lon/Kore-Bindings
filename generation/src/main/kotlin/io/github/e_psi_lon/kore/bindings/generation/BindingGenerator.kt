package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.KModifier
import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import io.github.e_psi_lon.kore.bindings.generation.poet.fileSpec
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists

class BindingGenerator(
    private val logger: Logger,
    private val datapack: Datapack,
    private val outputDir: Path,
    private val packageName: String,
    private val parentPackage: String,
    private val prefix: String?
) {

    /**
     * Generates all bindings for the datapack.
     *
     */
    operator fun invoke() {
        for (namespace in datapack.namespaces) {
            logger.info("Generating ${namespace.name} resources bindings")
            val namespaceFile = fileSpec(packageName, namespace.name.sanitizePascal()) {
                addAnnotation<Suppress> {
                    addMember("%S", "unused")
                    addMember("%S", "RedundantVisibilityModifier")
                    addMember("%S", "UnusedReceiverParameter")
                }
                objectBuilder(namespace.name.sanitizePascal()) {
                    property<String>("PATH") {
                        addModifiers(KModifier.CONST, KModifier.PRIVATE)
                        initializer("%S", "")
                    }

                    property<String>("namespace") {
                        addAnnotation<Suppress> {
                            addMember("%S", "ConstPropertyName")
                        }
                        addModifiers(KModifier.CONST)
                        initializer("%S", namespace.name)
                    }
                    for (component in namespace.components) {
                        val directoryHierarchy = component.directoryHierarchy.map { it.sanitizePascal() }
                        getOrCreateSubObjectBuilder(directoryHierarchy) {
                            if (!properties.containsKey("PATH")) {
                                property<String>("PATH") {
                                    addModifiers(KModifier.CONST, KModifier.PRIVATE)
                                    if (directoryHierarchy.isEmpty()) {
                                        initializer("%S", "")
                                    } else {
                                        val parentRef = calculateParentRef(directoryHierarchy, namespace)
                                        initializer("%P", $$"${$${parentRef}.PATH}$${component.directoryHierarchy.last()}/")
                                    }
                                }
                            }
                            when (component) {
                                is Component.Function -> {}
                                is Component.FunctionTag -> {}
                                is Component.Simple -> {
                                    property(
                                        component.fileName.sanitizeCamel(),
                                        component.componentType.returnType
                                    ) {
                                        getter {
                                            val type = component.componentType
                                            val parameters = type.parameters
                                            val nameToDefault = parameters.mapKeys { it.key.name }.toList()
                                            val isType = type.koreMethodOrClass is ClassOrMemberName.Class && type.koreMethodOrClass.name == type.returnType

                                            val callString = (if (isType) "%T(" else "%M(") +
                                                nameToDefault.joinToString(", ") { (_, value) -> getFormatPattern(value) } +
                                                ")"
                                            logger.debug("Generating getter for ${component.fileName} with template `$callString`")

                                            val args = buildList {
                                                if (isType) add(type.returnType) else add((type.koreMethodOrClass as ClassOrMemberName.Member).name)
                                                for ((paramName, paramValue) in nameToDefault) {
                                                    add(paramName)
                                                    add(getParameterValue(paramValue, namespace, component))
                                                }
                                            }
                                            logger.debug("Generated args: $args")
                                            addStatement("return $callString", *args.toTypedArray())
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (scoreboard in namespace.localScoreboards) {

                    }

                    for (storage in namespace.localStorages) {

                    }
                }
            }

            val outputPath = outputDir.resolve("${namespace.name.sanitizePascal()}.kt")
            if (!outputPath.exists()) outputPath.createFile()
            outputPath.bufferedWriter().use {
                namespaceFile.writeTo(it)
            }
        }
        for (group in datapack.namespaceGroups) {
            logger.info("Generating group ${group.prefix} resources bindings")

        }
    }

    private fun getParameterValue(source: ParameterValueSource, namespace: ParsedNamespace, component: Component): Any {
        return when (source) {
            is ParameterValueSource.Namespace -> namespace.name
            is ParameterValueSource.Name -> component.fileName
            is ParameterValueSource.Default<*> -> source.value
        }
    }

    /**
     * Determines the KotlinPoet format pattern for a parameter based on its value source.
     *
     * @param value The parameter's value source
     * @return Format pattern like "%L = %S" or "%L = %L" where:
     *         - %L = literal parameter name
     *         - %S = string literal value (for non-default values, adds quotes)
     *         - %L = literal value (for default values, rendered as-is)
     */
    private fun getFormatPattern(value: ParameterValueSource): String =
        if (value !is ParameterValueSource.Default<*>) "%L = %S"
        else "%L = %L"

    private fun calculateParentRef(directoryHierarchy: List<String>, namespace: ParsedNamespace) = directoryHierarchy.dropLast(1).let {
        when (it.size) {
            0 -> namespace.name.sanitizePascal()
            1 -> listOf(namespace.name.sanitizePascal(), it.first()).joinToString(".")
            else -> it.takeLast(2).joinToString(".")
        }
    }


    fun generateFunctionBindings(function: Component.Function, namespace: ParsedNamespace) {}
}
