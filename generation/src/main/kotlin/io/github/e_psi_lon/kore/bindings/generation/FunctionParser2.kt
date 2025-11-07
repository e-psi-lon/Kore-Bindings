package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Macro
import io.github.e_psi_lon.kore.bindings.generation.data.Scoreboard
import io.github.e_psi_lon.kore.bindings.generation.data.Storage
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension


// Regular expressions for extracting information from mcfunction files
// Note: setdisplay syntax is "setdisplay <slot> <objective>", so we need special handling
internal val scoreboardRegex = Regex("""^scoreboard\s+objectives\s+(?:(?:add|remove|modify)\s+([a-zA-Z0-9_.\-+]+)|setdisplay\s+[a-zA-Z0-9_.\-+]+(?:[^\S\r\n]+([a-zA-Z0-9_.\-+]+))?)(?:[^\S\r\n]|$)""", RegexOption.MULTILINE)
internal val storageRegex = Regex("""\bdata\s+(?:get|merge|remove|modify)\s+storage\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\b""")
internal val macroLineRegex = Regex("""^\$(.+)$""", RegexOption.MULTILINE)
internal val macroParameterRegex = Regex("""\$\(([a-zA-Z0-9_]+)\)""")

class FunctionParser2(
    private val code: String,
    private val namespaceName: String,
    private val relativePath: Path,
    private val logger: Logger
) {
    private fun preprocessMcFunction(input: String): String {
        return input.lineSequence()
            .map { stripCommentOutsideString(it).trim() }
            .filter { it.isNotEmpty() }
            .fold(StringBuilder()) { acc, line ->
                if (line.endsWith("\\")) {
                    acc.append(line.dropLast(1)).append(" ")
                } else {
                    acc.appendLine(line)
                }
                acc
            }.toString().trim().also { logger.debug("Preprocessed function $namespaceName:${relativePath.nameWithoutExtension}") }
    }

    /**
     * Parses all collected mcfunction files to extract scoreboards, storages, and macros
     */
    operator fun invoke(): Triple<Set<Scoreboard>, Set<Storage>, Macro?> {
        val fileContent = preprocessMcFunction(code)
        val scoreboards = extractScoreboards(fileContent)
        val storages = extractStorages(fileContent)
        val macro = extractMacro(fileContent)

        logger.debug("Parsed function $namespaceName:${relativePath.nameWithoutExtension}, found ${scoreboards.size} scoreboards, ${storages.size} storages, macro: ${macro != null}")
        if (macro != null) logger.debug("Macros are ${macro.parameters.joinToString(",")}")
        return Triple(scoreboards, storages, macro)
    }

    private fun extractScoreboards(fileContent: String): Set<Scoreboard> = scoreboardRegex.findAll(fileContent).mapNotNull { match ->
        // Group 1: add/remove/modify operations, Group 2: setdisplay operation
        val scoreboardName = match.groupValues[1].ifEmpty { match.groupValues[2] }

        // Skip if no objective name was captured (e.g., setdisplay without objective)
        if (scoreboardName.isEmpty()) {
            logger.debug("Skipping scoreboard match without objective name")
            return@mapNotNull null
        }

        logger.debug("Found scoreboard $scoreboardName")
        val parts = scoreboardName.split(".")

        // If scoreboard is unqualified (no dots), assume it belongs to current namespace
        return@mapNotNull if (parts.size == 1) {
            Scoreboard(scoreboardName, namespaceName)
        } else {
            // If qualified (has dots), the namespace is everything except the last part
            Scoreboard(
                scoreboardName,
                parts.dropLast(1).joinToString(".")
            )
        }
    }.toSet()

    private fun extractStorages(fileContent: String): Set<Storage> = storageRegex.findAll(fileContent).mapNotNull { match ->
        logger.debug("Found storage ${match.groupValues[1]}")
        match.groupValues[1].split(":").takeIf { it.size == 2 }
            // sourceNamespace (3rd param) is where this was declared, not the storage's namespace
            ?.let { (storageNamespace, name) -> Storage(storageNamespace, name, namespaceName) }
    }.toSet()

    private fun extractMacro(fileContent: String): Macro? {
        val parameters = mutableListOf<String>()
        macroLineRegex.findAll(fileContent).forEach { macroMatch ->
            val macroCommand = macroMatch.groupValues[1].trim()
            macroParameterRegex.findAll(macroCommand).forEach { paramMatch ->
                logger.debug("Found macro parameter ${paramMatch.groupValues[1]}")
                parameters.add(paramMatch.groupValues[1])
            }
        }

        return if (parameters.isNotEmpty()) {
            Macro(
                parameters = parameters.distinct()
            )
        } else null
    }
}

private fun stripCommentOutsideString(line: String): String {
    var inString = false
    for (i in line.indices) {
        val c = line[i]
        if (c in setOf('"', '\'')) inString = !inString
        if (c == '#' && !inString) return line.take(i)
    }
    return line
}