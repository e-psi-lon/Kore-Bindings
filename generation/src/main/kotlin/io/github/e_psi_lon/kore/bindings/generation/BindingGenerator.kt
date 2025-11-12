package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.DataArgument
import io.github.ayfri.kore.arguments.types.resources.FunctionArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.function
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
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtCompoundBuilder
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction4

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
                            is Component.FunctionTag -> generateFunctionTagBindings(component, namespace)
                            is Component.Simple -> generateRegularComponentBindings(component, namespace)
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

    private fun getParameterValue(source: ParameterValueSource, component: Component, namespace: ParsedNamespace, builder: TypeBuilder): Any {
        return when (source) {
            ParameterValueSource.Namespace -> "namespace"
            ParameterValueSource.Name -> $$"${PATH}$${component.fileName}"
            ParameterValueSource.SelfSafeReference -> component.fileName.sanitizeCamel().let { if (it.isValidKotlinIdentifier()) it else "n$it" }.let { builder.ensureNotDuplicatedName(it, component.componentType.duplicateSuffix) }
            ParameterValueSource.DataPack -> "dataPack"
            ParameterValueSource.Directory -> "${calculateSmartRef(component.directoryHierarchy, namespace)}.PATH"
            is ParameterValueSource.Type<*> -> source.value
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
            ParameterValueSource.DataPack -> "%L = %N"
            is ParameterValueSource.Type<*> -> "%L = %T"
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

    private fun TypeBuilder.generateRegularComponentBindings(component: Component.Simple, namespace: ParsedNamespace) {
        property(
            ensureNotDuplicatedName(component.fileName.sanitizeCamel(), component.componentType.duplicateSuffix, Type.PROPERTY),
            component.componentType.returnType
        ) {
            addDocs(*component.doc(namespace))
            initializer {
                val type = component.componentType
                val isType =
                    type.koreMethodOrClass is ClassOrMemberName.Class && type.koreMethodOrClass.name == type.returnType
                if (isType) add("%T(", type.returnType)
                else add("%M(", (type.koreMethodOrClass as ClassOrMemberName.Member).name)
                addParameters(component, namespace, builder = this@generateRegularComponentBindings)
                add(")")
            }
        }
    }

    private fun Component.doc(namespace: ParsedNamespace) = arrayOf("${componentType.name.lowercase()
        .replace('_', ' ')
        .capitalize()} reference for `$fileName` in namespace [${namespace.name}][${namespace.name.sanitizePascal()}.namespace]",
        "References `${namespace.name}:${fullPath}` in Minecraft."
    )

    enum class Type { PROPERTY, FUNCTION }
    private fun TypeBuilder.ensureNotDuplicatedName(name: String, duplicateSuffix: String = "1", type: Type = Type.PROPERTY) = if (
        when (type) {
            Type.PROPERTY -> hasDuplicateProperty(name)
            Type.FUNCTION -> hasDuplicateFunction(name)
        }) {
        logger.warn("Duplicate name: $name")
        "${name}$duplicateSuffix"
    } else name

    @OptIn(ExperimentalKotlinPoetApi::class)
    internal fun TypeBuilder.generateFunctionTagBindings(functionTag: Component.FunctionTag, namespace: ParsedNamespace) {
        val safeFunctionName = functionTag.fileName.sanitizeCamel()
        val contextParameterName = functionTag.componentType.requiredContext!!.simpleName.sanitizeCamel()
        val finalName = ensureNotDuplicatedName(safeFunctionName, functionTag.componentType.duplicateSuffix, Type.FUNCTION)
        function(finalName) {
            addDocs(*functionTag.doc(namespace))
            contextParameter(contextParameterName, functionTag.componentType.requiredContext)
            returns(functionTag.componentType.returnType)
            val block = codeBlock {
                add("%L.%M(", contextParameterName, (functionTag.componentType.koreMethodOrClass as ClassOrMemberName.Member).name)
                addParameters(functionTag, namespace, builder = this@generateFunctionTagBindings)
                add(")")
            }
            addStatement("return %L", block)
        }
    }

    private fun CodeBlock.Builder.addParameters(
        component: Component,
        namespace: ParsedNamespace,
        builder: TypeBuilder,
        componentListModifier: MutableList<Pair<String, ParameterValueSource>>.() -> Unit = { },
    ) {
        val parameterList = component.componentType.parameters.toList().toMutableList().apply {
            componentListModifier()
        }
        parameterList.forEachIndexed { index, (paramName, paramValue) ->
            add(getFormatPattern(paramValue), paramName, getParameterValue(paramValue, component, namespace, builder))
            if (index < parameterList.lastIndex) add(", ")
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class)
    private fun TypeBuilder.generateFunctionBindings(function: Component.Function, namespace: ParsedNamespace) {
        val functionName = function.fileName
        val selfRef = calculateSmartRef(function.directoryHierarchy, namespace)
        val safeFunctionName = ensureNotDuplicatedName(function.fileName.sanitizeCamel().let { if (it.isValidKotlinIdentifier()) it else "n$it" }, function.componentType.duplicateSuffix, Type.FUNCTION)
        val functionClassName = Function::class.asClassName()
        val koreClass = function.componentType.koreMethodOrClass as ClassOrMemberName.Class
        val contextParameterName = functionClassName.simpleName.sanitizeCamel()
        val kFunction: KFunction4<Function, String, Boolean, NbtCompound?, Command> = Function::function
        val kMember = kFunction.asMemberName()

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
                    add("%T(", FunctionWithMacros::class.asClassName())
                    addParameters(function, namespace, builder = this@generateFunctionBindings) {
                        // Rename "function" to "name"
                        set(
                            indexOfFirst { it.first == "function" },
                            "name" to ParameterValueSource.Name
                        )
                        add("macros" to ParameterValueSource.Type(clazzName))
                        add("datapack" to ParameterValueSource.DataPack)
                    }
                    add(")")
                }
            }
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                parameters.forEach { parameter ->
                    addParameter<String>(parameter.lowercase())
                }
                val block = codeBlock {
                    addStatement("%L.%M(", contextParameterName, kMember)
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
                    kMember,
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
                    kMember,
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
                initializer("%T(%L = %S, %L = %L, %L = %L)", koreClass.name, "name", functionName, "namespace", "namespace", "directory", "$selfRef.PATH")
            }
            function(safeFunctionName) {
                contextParameter(contextParameterName, functionClassName)
                returns(function.componentType.returnType)
                addStatement(
                    "return %L.%M(%L = %N)",
                    contextParameterName,
                    kMember,
                    "function",
                    safeFunctionName
                )
            }
        }
    }
}
