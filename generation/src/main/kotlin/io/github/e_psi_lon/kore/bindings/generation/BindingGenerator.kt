package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.FunctionArgument
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.generation.components.ClassOrMemberName
import io.github.e_psi_lon.kore.bindings.generation.components.ParameterValueSource
import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import io.github.e_psi_lon.kore.bindings.generation.poet.TypeBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.asMemberName
import io.github.e_psi_lon.kore.bindings.generation.poet.codeBlock
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
            generateNamespaceBindings(namespace)
        }
        for (group in datapack.namespaceGroups) {
            logger.info("Generating group ${group.prefix} resources bindings")
        }
    }

    private fun generateNamespaceBindings(namespace: ParsedNamespace) {
        val lazyFunc: KFunction1<() -> Any, Lazy<Any>> = ::lazy
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
                property<DataPack>("dataPack") {
                    addModifiers(KModifier.PRIVATE)
                    delegate(lazyFunc.asMemberName()) {
                        add("%T(%S)", DataPack::class.asClassName(), namespace.name)
                    }
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
                                    val parentRef = calculateSmartRef(component.directoryHierarchy, namespace, 1)
                                    initializer(
                                        "%P",
                                        $$"${$${parentRef}.PATH}$${component.directoryHierarchy.last()}/"
                                    )
                                }
                            }
                        }
                        when (component) {
                            is Component.Function -> generateFunctionBindings(component, namespace)
                            is Component.FunctionTag -> {}
                            is Component.Simple -> generateRegularComponentBindings(component)
                        }
                    }
                }
                for (scoreboard in namespace.localScoreboards) {
                    // TODO: Handle scoreboards
                    continue
                }

                for (storage in namespace.localStorages) {
                    // TODO: Handle storages
                    continue
                }
            }
        }

        val outputPath = outputDir.resolve("${namespace.name.sanitizePascal()}.kt")
        if (!outputPath.exists()) outputPath.createFile()
        outputPath.bufferedWriter().use {
            namespaceFile.writeTo(it)
        }
    }

    private fun getParameterValue(source: ParameterValueSource, component: Component): Any {
        return when (source) {
            is ParameterValueSource.Namespace -> "namespace"
            is ParameterValueSource.Name -> $$"${PATH}$${component.fileName}"
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
        when (value) {
            ParameterValueSource.Name -> "%L = %P"
            else -> "%L = %L"
        }

    private fun calculateSmartRef(directoryHierarchy: List<String>, namespace: ParsedNamespace, toDrop: Int = 0) =
        directoryHierarchy.map { it.sanitizePascal() }.dropLast(toDrop).let {
            when (it.size) {
                0 -> namespace.name.sanitizePascal()
                1 -> listOf(namespace.name.sanitizePascal(), it.first()).joinToString(".")
                else -> it.takeLast(2).joinToString(".")
            }
        }

    private fun TypeBuilder.generateRegularComponentBindings(component: Component.Simple) {
        property(
            component.fileName.sanitizeCamel(),
            component.componentType.returnType
        ) {
            getter {
                val type = component.componentType
                val parameters = type.parameters
                val nameToDefault = parameters.mapKeys { it.key.name }.toList()
                val isType =
                    type.koreMethodOrClass is ClassOrMemberName.Class && type.koreMethodOrClass.name == type.returnType
                val codeBlock = codeBlock {
                    if (isType) add("%T(", type.returnType)
                    else add("%M(", (type.koreMethodOrClass as ClassOrMemberName.Member).name)
                    nameToDefault.forEachIndexed { index, (paramName, paramValue) ->
                        add(getFormatPattern(paramValue), paramName, getParameterValue(paramValue, component))
                        if (index < nameToDefault.lastIndex) add(", ")
                    }
                    add(")")
                }
                addStatement("return %L", codeBlock)
            }
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class)
    internal fun TypeBuilder.generateFunctionBindings(function: Component.Function, namespace: ParsedNamespace) {
        val functionName = function.fileName
        var safeFunctionName = function.fileName.sanitizeCamel()
        if (!safeFunctionName.isValidKotlinIdentifier()) safeFunctionName = "n$safeFunctionName"
        val namespaceName = namespace.name
        val className = FunctionArgument::class.asClassName()
        property<FunctionArgument>(safeFunctionName) {
            initializer("%T(%S, %S, %L)", className, functionName, namespaceName, false)
        }
        function(safeFunctionName) {
            contextParameter("function", Function::class.asClassName())
            returns(function.componentType.returnType)
            val member = function.componentType.koreMethodOrClass as ClassOrMemberName.Member
            addStatement("return %M(%S, %S, %L)", member.name, functionName, namespaceName, false)
        }
    }
}
