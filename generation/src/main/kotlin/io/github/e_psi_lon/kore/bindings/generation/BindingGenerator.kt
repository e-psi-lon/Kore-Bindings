package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.DataArgument
import io.github.ayfri.kore.arguments.types.resources.FunctionArgument
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.FunctionWithMacros
import io.github.ayfri.kore.functions.Macros
import io.github.e_psi_lon.kore.bindings.generation.components.ClassOrMemberName
import io.github.e_psi_lon.kore.bindings.generation.components.ParameterValueSource
import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import io.github.e_psi_lon.kore.bindings.generation.poet.TypeBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.addParameter
import io.github.e_psi_lon.kore.bindings.generation.poet.asMemberName
import io.github.e_psi_lon.kore.bindings.generation.poet.codeBlock
import io.github.e_psi_lon.kore.bindings.generation.poet.fileSpec
import net.benwoodworth.knbt.NbtCompoundBuilder
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.reflect.KFunction1

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
                            is Component.FunctionTag -> generateFunctionTagBindings(component)
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
            is ParameterValueSource.SelfSafeReference -> component.fileName.sanitizeCamel().let { if (it.isValidKotlinIdentifier()) it else "n$it" }
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
                val isType =
                    type.koreMethodOrClass is ClassOrMemberName.Class && type.koreMethodOrClass.name == type.returnType
                val codeBlock = codeBlock {
                    if (isType) add("%T(", type.returnType)
                    else add("%M(", (type.koreMethodOrClass as ClassOrMemberName.Member).name)
                    addParameters(parameters, component)
                    add(")")
                }
                addStatement("return %L", codeBlock)
            }
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class)
    internal fun TypeBuilder.generateFunctionTagBindings(functionTag: Component.FunctionTag) {
        val safeFunctionName = functionTag.fileName.sanitizeCamel()
        val contextParameterName = functionTag.componentType.requiredContext!!.simpleName.sanitizeCamel()
        function(safeFunctionName) {
            contextParameter(contextParameterName, functionTag.componentType.requiredContext)
            returns(functionTag.componentType.returnType)
            val parameters = functionTag.componentType.parameters
            codeBlock {
                add("return %L.%M(", contextParameterName, (functionTag.componentType.koreMethodOrClass as ClassOrMemberName.Member).name)
                addParameters(parameters, functionTag)
                add(")")
            }
        }
    }

    private fun CodeBlock.Builder.addParameters(
        parameters: Map<String, ParameterValueSource>,
        component: Component
    ) {
        val parameterList = parameters.toList()
        parameterList.forEachIndexed { index, (paramName, paramValue) ->
            add(getFormatPattern(paramValue), paramName, getParameterValue(paramValue, component))
            if (index < parameterList.lastIndex) add(", ")
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class)
    internal fun TypeBuilder.generateFunctionBindings(function: Component.Function, namespace: ParsedNamespace) {
        val functionName = function.fileName
        val selfRef = calculateSmartRef(function.directoryHierarchy, namespace)
        val safeFunctionName = function.fileName.sanitizeCamel().let { if (it.isValidKotlinIdentifier()) it else "n$it" }
        val functionArgumentClassName = FunctionArgument::class.asClassName()
        val functionClassName = Function::class.asClassName()
        val member = function.componentType.koreMethodOrClass as ClassOrMemberName.Member
        val contextParameterName = functionClassName.simpleName.sanitizeCamel()

        if (function.macro?.hasParameters == true) {
            val parameters = function.macro.parameters
            val functionWithMacros = FunctionWithMacros::class.asClassName()
            val clazz = classBuilder("${safeFunctionName.capitalize()}Macro") {
                superclass(Macros::class.asClassName())
                parameters.forEach { parameter ->
                    property<String>(parameter.sanitizeCamel()) {
                        delegate("%S", parameter)
                    }
                }
            }
            val clazzName = ClassName("", clazz.name)
            val typedFunctionWithMacros = functionWithMacros.parameterizedBy(clazzName)
            property(safeFunctionName, typedFunctionWithMacros) {
                val lazyFunc: KFunction1<() -> FunctionWithMacros<*>, Lazy<FunctionWithMacros<*>>> = ::lazy
                delegate(lazyFunc.asMemberName()) {
                    add(
                        "%T(%L = %S, %L = ::%T, %L = %L, %L = %L, %L = %N)",
                        FunctionWithMacros::class.asClassName(),
                        "name", functionName,
                        "macros", clazzName,
                        "namespace", "namespace",
                        "directory", "$selfRef.PATH",
                        "datapack", "dataPack"
                    )
                }
            }
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                parameters.forEach { parameter ->
                    addParameter<String>(parameter.lowercase())
                }
                val block = codeBlock {
                    addStatement("%L.%M(", contextParameterName, member.name)
                    withIndent {
                        addStatement("%L = %N", "function", safeFunctionName)
                        beginControlFlow(")")
                        parameters.forEach { parameter ->
                            addStatement("this[%S] = %L", parameter, parameter.lowercase())
                        }
                        endControlFlow()
                    }
                    add(")")
                }
                addStatement("return %L", block)
            }

            // NBT tag overload
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                addParameter<NbtCompoundBuilder.() -> Unit>("nbt") { defaultValue("%L", "{}") }
                addStatement(
                    "return %L.%M(%L = %L, %L = %L)",
                    contextParameterName,
                    member.name,
                    "function",
                    safeFunctionName,
                    "builder",
                    "nbt"
                )

            }

            // DataArgument overload
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                addParameter<DataArgument>("arguments")
                addParameter<String?>("path") { defaultValue("%L", null) }
                addStatement(
                    "return %L.%M(%L = %L, %L = %L, %L = %L)",
                    contextParameterName,
                    member.name,
                    "function",
                    safeFunctionName,
                    "arguments",
                    "arguments",
                    "path",
                    "path"
                )
            }
        } else {
            property<FunctionArgument>(safeFunctionName) {
                initializer("%T(%L = %S, %L = %L, %L = %L)", functionArgumentClassName, "name", functionName, "namespace", "namespace", "directory", "$selfRef.PATH")
            }
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                addStatement(
                    "return %L.%M(%L = %N)",
                    contextParameterName,
                    member.name,
                    "function",
                    safeFunctionName
                )
            }
        }
    }
}
