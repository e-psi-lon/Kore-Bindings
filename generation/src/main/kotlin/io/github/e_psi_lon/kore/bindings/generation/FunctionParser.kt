package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.ayfri.kore.arguments.Argument
import io.github.ayfri.kore.arguments.scores.ExecuteScore as ArgExecuteScore
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.scores.Scores
import io.github.ayfri.kore.arguments.scores.SelectorScore
import io.github.ayfri.kore.arguments.types.ScoreHolderArgument
import io.github.ayfri.kore.arguments.types.resources.StorageArgument
import io.github.ayfri.kore.commands.execute.ExecuteCondition
import io.github.ayfri.kore.commands.execute.ExecuteStore
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.arguments.types.DataArgument
import io.github.e_psi_lon.kore.bindings.generation.poet.FileBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.TypeBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.addDocs
import net.benwoodworth.knbt.NbtCompound
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo

// Regular expressions for extracting information from mcfunction files
private val scoreboardRegex = Regex("""\bscoreboard\s+objectives\s+(?:add|remove|setdisplay|modify)\s+([a-zA-Z0-9_.\-+]+)\b""")
private val storageRegex = Regex("""\bdata\s+(?:get|merge|remove|modify)\s+storage\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\b""")
private val macroLineRegex = Regex("""^\$(.+)$""", RegexOption.MULTILINE)
private val macroParameterRegex = Regex("""\$\(([a-zA-Z0-9_]+)\)""")

/**
 * A class that parses Minecraft function files to extract various elements for binding generation.
 * This includes scoreboards, storages, and now macros.
 */
class FunctionParser(
    val logger: Logger,
    private val namespace: File,
    private val parentPackage: String,
    // Used for prefixed namespaces such as prefix.something which define stuff under "prefix" and not "prefix.something"
    private val prefix: String?
) {
    private val scoreboards = mutableSetOf<String>()
    private val storages = mutableSetOf<Pair<String, String>>()
    private val functions = mutableSetOf<Path>()
    private val macros = mutableMapOf<String, MacroDefinition>()

    /**
     * Represents a parsed macro definition from a Minecraft function.
     */
    data class MacroDefinition(
        val functionPath: String,
        val functionName: String,
        val parameters: List<String>,
        val originalCommand: String
    )

    /**
     * Collects all .mcfunction files in the namespace
     */
    private fun collectFiles() {
        val functionDir = namespace.resolve("function")
        if (functionDir.exists() && functionDir.isDirectory) {
            functions.addAll(
                functionDir.walkTopDown()
                    .filter { it.isFile && it.extension == "mcfunction" }
                    .map { it.toPath() }
                    .toList()
            )
        }
    }

    /**
     * Main entry point for processing. Parses all functions and generates bindings.
     */
    internal operator fun invoke(typeBuilder: TypeBuilder, file: FileBuilder) {
        // Parse all function files
        parseMcFunctions()

        // Add necessary imports for macros
        file.addImport(ClassName("io.github.ayfri.kore.commands", "function"))
        file.addImport(DataArgument::class.asClassName())
        file.addImport(NbtCompound::class.asClassName())
        file.addImport(ClassName("io.github.ayfri.kore.utils", "nbt"))

        // Add the scoreboards to the type builder
        typeBuilder.apply {
            // Process scoreboards
            processScoreboards(this, file)

            // Process storages
            processStorages(this, file)

            // Process macros
            processMacros(this)
        }
    }

    /**
     * Process scoreboards and generate appropriate bindings
     */
    private fun processScoreboards(typeBuilder: TypeBuilder, file: FileBuilder) {
        scoreboards.forEach { scoreboard ->
            logger.debug("Processing scoreboard: $scoreboard")
            val parts = scoreboard.split(".")
            file.addImport(ClassName("io.github.ayfri.kore.arguments.scores", "score"))
            if (parts[0] != namespace.name) {
                if (parts.size == 1) {
                    file.scoreBoard(scoreboard)
                } else if (parts.size == 2 && parts[0] == prefix) {
                    file.scoreBoard(
                        scoreboard = parts[1],
                        parent = ClassName(parentPackage, prefix.sanitizePascal())
                    )
                } else {
                    return@forEach
                }
            } else {
                var currentBuilder = typeBuilder
                parts.drop(1).dropLast(1).forEach { part ->
                    currentBuilder = currentBuilder.objectBuilder(part.sanitizePascal())
                }
                currentBuilder.scoreBoard(parts.joinToString("."))
            }
        }
    }

    /**
     * Process storages and generate appropriate bindings
     */
    private fun processStorages(typeBuilder: TypeBuilder, file: FileBuilder) {
        val className = ClassName("io.github.ayfri.kore.arguments.types.resources", "storage")
        file.addImport(className)
        storages.forEach { (storageNamespace, name) ->
            logger.debug("Processing storage: $storageNamespace:$name")
            val parts = storageNamespace.split(".")
            if (parts[0] != namespace.name && storageNamespace != namespace.name) {
                if (parts.size == 1) {
                    // Always escape property names with backticks for consistency with KotlinPoet
                    val propertyName = "`${name.sanitizeCamel()}`"
                    file.property<StorageArgument>(propertyName) {
                        getter {
                            addStatement("return storage(%S, %S)", name, storageNamespace)
                        }
                    }
                } else if (parts[0] == prefix) {
                    // Extract module name from namespace (e.g., "bs.math" -> "Math")
                    val moduleName = parts.drop(1).joinToString("") { it.sanitizePascal() }
                    // Create property name with module suffix to avoid conflicts (e.g., "ctxMath")
                    val propertyName = "${name.sanitizeCamel()}$moduleName"
                    
                    file.property<StorageArgument>(propertyName) {
                        receiver(ClassName(parentPackage, prefix.sanitizePascal()))
                        addDocs(
                            "Storage reference for `$name` in namespace `$storageNamespace`.",
                            "",
                            "Note: In Minecraft and in generated functions, this storage is referenced as `$storageNamespace:$name`.",
                            "",
                            "Property name includes module suffix (`$moduleName`) to avoid conflicts with same-named storages in other modules."
                        )
                        getter {
                            addStatement("return storage(%S, %S)", name, storageNamespace)
                        }
                    }
                    file.addImport(ClassName("io.github.ayfri.kore.arguments.scores", "score"))
                } else {
                    file.property<StorageArgument>("${parts.joinToString { it.sanitizePascal() }}${name.sanitizePascal()}".sanitizeCamel() ) {
                        addDocs(
                            "Storage reference for `$name` in namespace `$storageNamespace`.", "",
                            "Note: In Minecraft and in generated functions, this storage is referenced as `$storageNamespace:$name`."
                        )
                        getter {
                            addStatement("return storage(%S, %S)", name, storageNamespace)
                        }
                    }
                }
            } else {
                var currentBuilder = typeBuilder
                for (part in parts.drop(1).dropLast(1)) {
                    currentBuilder = currentBuilder.objectBuilder(part.sanitizePascal())
                }
                val finalName =
                    if (currentBuilder.properties.containsKey(name)) name + "Storage"
                    else name
                currentBuilder.property<StorageArgument>(finalName.sanitizeCamel()) {
                    getter {
                        addStatement("return storage(%S, %S)", name, storageNamespace)
                    }
                }
            }
        }
    }

    /**
     * Process macros and generate appropriate bindings
     */
    private fun processMacros(typeBuilder: TypeBuilder) {
        macros.forEach { (macroPath, macro) ->
            logger.debug("Processing macro: ${macro.functionPath} with parameters: ${macro.parameters.joinToString(", ")}")
            // Extract path parts to create nested objects if needed
            val pathParts = macroPath.split("/").dropLast(1)
            var currentBuilder = typeBuilder

            // Navigate to or create the appropriate nested objects for the macro
            pathParts.forEach { part ->
                if (part.isNotEmpty()) {
                    currentBuilder = currentBuilder.objectBuilder(part.sanitizePascal())
                }
            }

            // Generate the three binding functions for this macro
            generateMacroBindings(currentBuilder, macro)
        }
    }

    /**
     * Generate the three binding functions for a macro:
     * 1. With typed parameters
     * 2. With DataArgument parameter
     * 3. With NbtCompound parameter
     */
    private fun generateMacroBindings(typeBuilder: TypeBuilder, macro: MacroDefinition) {
        val functionName = macro.functionName.sanitizeCamel()
        val namespaceName = namespace.name

        // Check if we already have a function with this name
        if (typeBuilder.functions.containsKey(functionName)) {
            return
        }

        // 1. Create the function with typed parameters
        if (macro.parameters.isNotEmpty()) {
            typeBuilder.function(functionName) {
                returns(Command::class.asClassName())

                // Add each parameter
                macro.parameters.forEach { param ->
                    addParameter(param, String::class)
                }

                addStatement(
                    "return function(name = \"\${PATH}%L\", namespace = namespace, arguments = nbt {",
                    macro.functionName
                )

                // Add each parameter to the NBT
                macro.parameters.forEach { param ->
                    addStatement("    this[\"%L\"] = %L", param, param)
                }

                addStatement("})")

                addKdoc(
                    "Generated binding for macro function `$namespaceName:${macro.functionPath}`\n" +
                    "\n" +
                    "Original command: `${macro.originalCommand}`\n" +
                    "\n" +
                    "Parameters:\n" +
                    macro.parameters.joinToString("\n") { "* `$it`" }
                )
            }
        }

        // 2. Create the function with DataArgument parameter
        typeBuilder.function(functionName) {
            returns(Command::class.asClassName())

            addParameter("arguments", DataArgument::class.asClassName())
            addParameter(
				ParameterSpec
					.builder("path", String::class.asClassName().copy(nullable = true))
					.defaultValue("%L", null)
					.build()
			)

            addStatement(
                "return %T(name = \"\${PATH}%L\", namespace = namespace, arguments = arguments, path = path)",
                ClassName("io.github.ayfri.kore.commands", "function"),
                macro.functionName
            )

            if (macro.parameters.isEmpty()) {
                addDocs(
                    "Generated binding for function `$namespaceName:${macro.functionPath}`", "",
                    "Original command: `${macro.originalCommand}`"
                )
            } else {
                addDocs(
                    "Generated binding for macro function `$namespaceName:${macro.functionPath}`", "",
                    "Original command: `${macro.originalCommand}`", "",
                    "Expected arguments format:",
                    macro.parameters.joinToString("\n") { "* `$it`: String" }
                )
            }
        }

        // 3. Create the function with NbtCompound parameter
        typeBuilder.function(functionName) {
            returns(Command::class.asClassName())

			addParameter(
				ParameterSpec
					.builder("arguments", NbtCompound::class.asClassName().copy(nullable = true))
					.defaultValue("%L", null)
					.build()
			)

            addStatement(
                "return function(name = \"\${PATH}%L\", namespace = namespace, arguments = arguments)",
                macro.functionName
            )

            if (macro.parameters.isEmpty()) {
                addDocs(
                    "Generated binding for function `$namespaceName:${macro.functionPath}`",
                    "",
                    "Original command: `${macro.originalCommand}`"
                )
            } else {
                addDocs(
                    "Generated binding for macro function `$namespaceName:${macro.functionPath}`", "",
                    "Original command: `${macro.originalCommand}`", "",
                    "Expected NBT format:",
                    macro.parameters.joinToString("\n") { "* `$it`: String" }
                )
            }
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class, DelicateKotlinPoetApi::class)
    private fun TypeBuilder.scoreBoard(scoreboard: String) {
        val finalName =
            (if (properties.containsKey(scoreboard)) scoreboard.split(".").last() + "Scoreboard"
            else scoreboard.split(".").last()).sanitizeCamel()
        val score = Scores::class.asClassName()
        val selectorScore = SelectorScore::class.asClassName()
        val receiver = score.parameterizedBy(selectorScore)
        function(finalName) {
            contextParameter("scores", receiver)
            returns(selectorScore)
            addStatement("return score(%S)", scoreboard)
        }
        property<String>(finalName) {
            addModifiers(KModifier.CONST)
            addAnnotation<Suppress> {
                addMember("%S", "ConstPropertyName")
            }
            initializer("%S", scoreboard)
        }
        fun scoreboardFunction(parameterType: Class<*>) {
            function(finalName) {
                contextParameter("scores", receiver)
                addParameter("value", parameterType)
                addStatement("return score(%S, value)", scoreboard)
            }
        }
        scoreboardFunction(Int::class.java)
        scoreboardFunction(IntRange::class.java)
        scoreboardFunction(IntRangeOrInt::class.java)
        fun scoreBoardExecute(contextParameterClass: Class<*>, returnType: TypeName) {
            function(finalName) {
                contextParameter("execute", contextParameterClass.asClassName())
                addParameter("target", ScoreHolderArgument::class.asClassName())
                returns(returnType)
                addStatement("return score(target, %S)", scoreboard)
            }
        }
        scoreBoardExecute(ExecuteStore::class.java, List::class.asClassName().parameterizedBy(Argument::class.asClassName()))
        scoreBoardExecute(ExecuteCondition::class.java, ArgExecuteScore::class.asClassName())
    }

    @OptIn(ExperimentalKotlinPoetApi::class, DelicateKotlinPoetApi::class)
    private fun FileBuilder.scoreBoard(scoreboard: String, parent: ClassName? = null) {
        val finalName = (
            if (propertySpecs.containsKey(scoreboard)) scoreboard + "Scoreboard"
            else scoreboard).sanitizeCamel()
        val score = Scores::class.asClassName()
        val selectorScore = SelectorScore::class.asClassName()
        val receiver = score.parameterizedBy(selectorScore)
        function(finalName) {
            if (parent != null) {
                receiver(parent)
                contextParameter("scores", receiver)
            } else {
                receiver(receiver)
            }
            returns(selectorScore)
            addStatement("return score(%S)", scoreboard)
        }
        property<String>(finalName) {
            if (parent != null) {
                receiver(parent)
                getter {
                    addStatement("return %S", scoreboard)
                }
            } else {
                addModifiers(KModifier.CONST)
                addAnnotation<Suppress> {
                    addMember("%S", "ConstPropertyName")
                }
                initializer("%S", scoreboard)
            }
        }
        fun scoreboardFunction(parameterType: Class<*>) {
            function(finalName) {
                if (parent != null) {
                    receiver(parent)
                    contextParameter("scores", receiver)
                } else {
                    receiver(receiver)
                }
                addParameter("value", parameterType)
                addStatement("return score(%S, value)", scoreboard)
            }
        }
        scoreboardFunction(Int::class.java)
        scoreboardFunction(IntRange::class.java)
        scoreboardFunction(IntRangeOrInt::class.java)
        fun scoreBoardExecute(contextParameterClass: Class<*>, returnType: TypeName) {
            function(finalName) {
                if (parent != null) {
                    receiver(parent)
                    contextParameter("execute", contextParameterClass.asClassName())
                } else {
                    receiver(contextParameterClass.asClassName())
                }
                returns(returnType)
                addParameter("target", ScoreHolderArgument::class.asClassName())
                addStatement("return score(target, %S)", scoreboard)
            }
        }
        scoreBoardExecute(ExecuteStore::class.java, List::class.asClassName().parameterizedBy(Argument::class.asClassName()))
        scoreBoardExecute(ExecuteCondition::class.java, ArgExecuteScore::class.asClassName())
    }

    /**
     * Parses all collected mcfunction files to extract scoreboards, storages, and macros
     */
    private fun parseMcFunctions() {
        collectFiles()

        val functionDir = namespace.resolve("function").toPath()

        functions.forEach { filePath ->
            val fileContent = File(filePath.toString()).readText()
            val processed = preprocessMcFunction(fileContent)

            // Extract scoreboards
            scoreboards.addAll(
                scoreboardRegex.findAll(processed)
                    .map { it.groupValues[1] }
            )

            // Extract storages
            storages.addAll(
                storageRegex.findAll(processed).mapNotNull { match ->
                    match.groupValues[1].split(":").takeIf { it.size == 2 }
                        ?.let { (namespace, name) -> Pair(namespace, name) }
                }
            )

            // Extract macros
            val relativePath = filePath.relativeTo(functionDir).toString()
                .replace('\\', '/') // Normalize path separators
                .removeSuffix(".mcfunction")

            // Find macro lines (starting with $)
            macroLineRegex.findAll(processed).forEach { macroMatch ->
                val macroCommand = macroMatch.groupValues[1].trim()

                // Find all macro parameters $(param_name)
                val parameterNames = mutableListOf<String>()
                macroParameterRegex.findAll(macroCommand).forEach { paramMatch ->
                    parameterNames.add(paramMatch.groupValues[1])
                }

                // Only add if it has parameters or if we don't already have this macro
                if (parameterNames.isNotEmpty() || !macros.containsKey(relativePath)) {
                    val fileName = filePath.name.removeSuffix(".mcfunction").sanitizeCamel()
                    macros[relativePath] = MacroDefinition(
                        functionPath = relativePath,
                        functionName = fileName,
                        parameters = parameterNames.distinct(),
                        originalCommand = macroCommand
                    )
                }
            }
        }
    }

    /**
     * Preprocesses an mcfunction file content:
     * - Removes comments
     * - Handles line continuations
     * - Cleans up empty lines
     */
    private fun preprocessMcFunction(input: String): String {
        return input.lineSequence()
            .map { it.replace(Regex("#.*"), "").trim() }
            .filter { it.isNotEmpty() }
            .fold(StringBuilder()) { acc, line ->
                if (line.endsWith("\\")) {
                    acc.append(line.dropLast(1)).append(" ")
                } else {
                    acc.appendLine(line)
                }
                acc
            }.toString().trim()
    }
}
