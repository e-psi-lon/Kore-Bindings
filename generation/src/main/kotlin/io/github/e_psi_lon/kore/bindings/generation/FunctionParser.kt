package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.scores.Scores
import io.github.ayfri.kore.arguments.scores.SelectorScore
import io.github.ayfri.kore.arguments.types.resources.StorageArgument
import io.github.e_psi_lon.kore.bindings.generation.poet.FileBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.TypeBuilder
import java.io.File


private val scoreboardRegex = Regex("""\bscoreboard\s+objectives\s+(?:add|remove|setdisplay)\s+([a-zA-Z0-9_.]+)\b""")
private val storageRegex = Regex("""\bdata\s+(?:get|merge|remove|modify)\s+storage\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\b""")

class FunctionParser(
	private val namespace: File,
	private val parentPackage: String,
	// Used for prefixed namespaces such as prefix.something which define stuff under "prefix" and not "prefix.something"
	private val prefix: String?
) {
	private val scoreboards = mutableSetOf<String>()
	private val storages = mutableSetOf<Pair<String, String>>()
	private val functions = mutableSetOf<File>()

	private fun collectFiles() {
		namespace.resolve("function").walkTopDown().filter { it.isFile && it.extension == "mcfunction" }.forEach { functions.add(it) }
	}

	internal operator fun invoke(typeBuilder: TypeBuilder, file: FileBuilder) {
		// Parse the functions
		parseMcFunction()

		// Add the scoreboards to the type builder
		typeBuilder.apply {
			scoreboards.forEach { scoreboard ->
				val parts = scoreboard.split(".")
				file.addImport(ClassName("io.github.ayfri.kore.arguments.scores", "score"))
				if (parts[0] != namespace.name) {
					if (parts.size == 1) file.scoreBoard(scoreboard)
					else if (parts.size == 2 && parts[0] == prefix) {
						file.scoreBoard(
							parts[1],
							ClassName(parentPackage, prefix.sanitizePascal())
						)
					}
					else return@forEach
				} else {
					var currentBuilder = this
					parts.drop(1).dropLast(1).forEach { part ->
						currentBuilder = currentBuilder.objectBuilder(part.sanitizePascal())
					}
					currentBuilder.scoreBoard(parts.joinToString("."))
				}
			}
			storages.forEach { (storageNamespace, name) ->
				val parts = storageNamespace.split(".")
				if (parts[0] != namespace.name && storageNamespace != namespace.name) {
					if (parts.size == 1) {
						file.property<StorageArgument>(name.sanitizeCamel()) {
							addDocs(
								"Storage reference for `$name` in namespace `$storageNamespace`.",
								"",
								"Note: In Minecraft and in generated functions, this storage is referenced as `$storageNamespace:$name`."
							)
							getter {
								addStatement("return io.github.ayfri.kore.arguments.types.resources.storage(%S, %S)", name, storageNamespace)
							}
						}
					} else if (parts[0] == prefix) {
						file.property<StorageArgument>("${parts.drop(1).joinToString { it.sanitizePascal() }}${name.sanitizePascal()}".sanitizeCamel()) {
							receiver(ClassName(parentPackage, prefix.sanitizePascal()))
							addDocs(
								"Storage reference for `$name` in namespace `$storageNamespace`.",
								"",
								"Note: In Minecraft and in generated functions, this storage is referenced as `$storageNamespace:$name`."
							)
							getter {
								addStatement(
									"return io.github.ayfri.kore.arguments.types.resources.storage(%S, %S)",
									name,
									storageNamespace
								)
							}
						}
					} else {
						file.property<StorageArgument>("${parts.joinToString { it.sanitizePascal() }}${name.sanitizePascal()}".sanitizeCamel() ) {
							addDocs(
								"Storage reference for `$name` in namespace `$storageNamespace`.",
								"",
								"Note: In Minecraft and in generated functions, this storage is referenced as `$storageNamespace:$name`."
							)
							getter {
								addStatement(
									"return io.github.ayfri.kore.arguments.types.resources.storage(%S, %S)",
									name,
									storageNamespace
								)
							}
						}
					}
				} else {
					var currentBuilder = this
					for (part in parts.drop(1).dropLast(1)) {
						currentBuilder = currentBuilder.objectBuilder(part)
					}
					val finalName =
						if (properties.containsKey(name)) name + "Storage"
						else name
					currentBuilder.property<StorageArgument>(finalName.sanitizeCamel()) {
						getter {
							addStatement(
								"return io.github.ayfri.kore.arguments.types.resources.storage(%S, %S)",
								name,
								storageNamespace
							)
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalKotlinPoetApi::class)
	private fun TypeBuilder.scoreBoard(scoreboard: String) {
		val finalName =
			(if (properties.containsKey(scoreboard)) scoreboard.split(".").last() + "Scoreboard"
			else scoreboard.split(".").last()).sanitizeCamel()
		val score = Scores::class.asClassName()
		val selectorScore = SelectorScore::class.asClassName()
		val receiver = score.parameterizedBy(selectorScore)
		function(finalName) {
			contextReceivers(receiver)
			returns(selectorScore)
			addStatement("return score(%S)", scoreboard)
		}
		property<String>(finalName) {
			initializer("%S", scoreboard)
		}
		fun scoreboardFunction(parameterType: Class<*>) {
			function(finalName) {
				contextReceivers(receiver)
				addParameter("value", parameterType)
				addStatement("return score(%S, value)", scoreboard)
			}
		}
		scoreboardFunction(Int::class.java)
		scoreboardFunction(IntRange::class.java)
		scoreboardFunction(IntRangeOrInt::class.java)
	}

	@OptIn(ExperimentalKotlinPoetApi::class)
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
				contextReceivers(receiver)
			} else {
				receiver(receiver)
			}
			returns(selectorScore)
			addStatement("return score(%S)", scoreboard)
		}
		property<String>(finalName) {
			if (parent != null) {
				receiver(parent)
				getter { addStatement("return %S", scoreboard) }
			} else {
				initializer("%S", scoreboard)
			}
		}
		fun scoreboardFunction(parameterType: Class<*>) {
			function(finalName) {
				if (parent != null) {
					receiver(parent)
					contextReceivers(receiver)
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
	}



	private fun parseMcFunction() {
		collectFiles()
		functions.forEach { file ->
			val processed = preprocessMcFunction(file.readLines().joinToString("\n"))
			scoreboards.addAll(
				scoreboardRegex.findAll(processed)
					.map { it.groupValues[1] }
			)
			storages.addAll(
				storageRegex.findAll(processed).mapNotNull { it ->
					it.groupValues[1].split(":").takeIf { it.size == 2 }
						?.let { (namespace, name) -> Pair(namespace, name) }
				}
			)
		}
	}

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
