package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.utils.pascalCase
import io.github.e_psi_lon.kore.bindings.generation.poet.*
import java.io.File
import java.util.zip.ZipFile

/**
 * Generates Kotlin Kore DSL bindings for a Minecraft datapack.
 *
 * This class takes a folder or zip file containing a Minecraft datapack and generates Kotlin bindings
 * for all datapack's components. The generated bindings are written to the specified output directory.
 *
 * @param folder The folder containing the datapack. If null, the zipFile parameter must be provided.
 * @param zipFile The zip file containing the datapack. If null, the folder parameter must be provided.
 * @param outputDir The directory where the generated bindings will be written.
 * @param packageName The package name for the generated bindings.
 * @param parentPackage The parent package containing objects definitions for scoreboards and storages.
 * Defaults to the same as packageName.
 * @param verbose If true, prints additional information during processing.
 *
 * @throws IllegalArgumentException If neither folder nor zipFile is provided, or if both are provided.
 * Can also be thrown if the provided folder or zip file is invalid.
 *
 */
@OptIn(ExperimentalKotlinPoetApi::class)
class GenerateDatapackBindings(
	val folder: File? = null,
	val zipFile: File? = null,
	val outputDir: File,
	val packageName: String,
	private val parentPackage: String,
	private val verbose: Boolean = false
) {
	init {
		if (folder == null && zipFile == null) {
			throw IllegalArgumentException("Either folder or zipFile must be provided")
		}

		if (folder != null && zipFile != null) {
			throw IllegalArgumentException("Only one of folder or zipFile must be provided")
		}

		if (folder != null) {
			handleDatapack(folder)
		} else {
			val tempFolder = File.createTempFile("datapack", "extract")
			tempFolder.delete()
			tempFolder.mkdirs()
			// Extract zip file
			ZipFile(zipFile!!).use { zip ->
				zip.entries().asSequence().forEach { entry ->
					val entryFile = File(tempFolder, entry.name)
					if (entry.isDirectory) {
						entryFile.mkdirs()
					} else {
						entryFile.parentFile.mkdirs()
						entryFile.outputStream().use { output ->
							zip.getInputStream(entry).copyTo(output)
						}
					}
				}
			}
			handleDatapack(tempFolder)
		}
	}

	private fun handleDatapack(folder: File) {
		val dataFolder = folder.resolve("data")
		if (!folder.resolve("pack.mcmeta").exists() || !dataFolder.exists() || !dataFolder.isDirectory)
			throw IllegalArgumentException("Invalid datapack folder: ${folder.absolutePath}")
		val namespaceGroups = mutableMapOf<String, MutableList<String>>()

		for (namespace in dataFolder.listFiles { file -> file.isDirectory }!!) {
			val namespaceName = namespace.name
			if (namespaceName.contains('.')) {
				val parts = namespaceName.split('.', limit = 2)
				val prefix = parts[0]

				namespaceGroups.getOrPut(prefix) { mutableListOf() }.add(namespaceName)
			} else {
				processNamespace(namespace, namespaceName)
			}
		}

		for ((prefix, namespaces) in namespaceGroups) {
			val capitalizedPrefix = prefix.sanitizePascal()

			for (fullNamespace in namespaces) {
				val namespaceSuffix = fullNamespace.substringAfter('.')
				val capitalizedSuffix = namespaceSuffix.sanitizePascal()
				val fullCapitalized = "${capitalizedPrefix}${capitalizedSuffix}"

				val namespaceFolder = dataFolder.resolve(fullNamespace)
				if (!namespaceFolder.exists() || !namespaceFolder.isDirectory) continue

				val suffixFile = fileSpec(packageName, fullCapitalized) {
					addAnnotation<Suppress> {
						addMember("%S", "unused")
						addMember("%S", "RedundantVisibilityModifier")
						addMember("%S", "UnusedReceiverParameter")
					}
					// Create main object for the namespace
					objectBuilder(fullCapitalized) {
						// Check if object name might not be a valid Kotlin identifier
						if (!isValidKotlinIdentifier(fullCapitalized)) {
							addAnnotation<Suppress> {
								addMember("%S", "ClassName")
							}
						}
						
						property<String>("namespace") {
							addAnnotation<Suppress> {
								addMember("%S", "ConstPropertyName")
							}
							addModifiers(KModifier.CONST)
							initializer("%S", fullNamespace)
						}
						
						// Only add PATH property if it doesn't exist yet
						if (!properties.containsKey("PATH")) {
							property<String>("PATH") {
								addModifiers(KModifier.CONST, KModifier.PRIVATE)
								initializer("%S", "")
							}
						}
						
						// Process each datapack component type
						for (dpComponent in DatapackComponentType.values()) {
							val componentFolder = namespaceFolder.resolve(dpComponent.folderName)
							if (componentFolder.exists() && componentFolder.isDirectory) {
								if (verbose) println("Adding ${dpComponent.name.lowercase()} in namespace $fullNamespace")
								handleComponent(dpComponent, componentFolder, fullNamespace)
							}
						}
						val functionParser = FunctionParser(namespaceFolder, parentPackage, prefix)
						functionParser(this, this@fileSpec)
					}
				}
				suffixFile.writeTo(outputDir)
			}
		}
	}

	private fun processNamespace(namespace: File, namespaceName: String) {
		val capitalizedName = namespaceName.sanitizePascal()
		val file = fileSpec(packageName, capitalizedName) {
			addAnnotation<Suppress> {
				addMember("%S", "unused")
				addMember("%S", "RedundantVisibilityModifier")
				addMember("%S", "UnusedReceiverParameter")
			}
			objectBuilder(capitalizedName) {
				// Check if object name might not be a valid Kotlin identifier
				if (!isValidKotlinIdentifier(capitalizedName)) {
					addAnnotation<Suppress> {
						addMember("%S", "ClassName")
					}
				}
				
				property<String>("namespace") {
					addAnnotation<Suppress> {
						addMember("%S", "ConstPropertyName")
					}
					addModifiers(KModifier.CONST)
					initializer("%S", namespaceName)
				}
				
				// Only add PATH property if it doesn't exist yet
				if (!properties.containsKey("PATH")) {
					property<String>("PATH") {
						addModifiers(KModifier.CONST, KModifier.PRIVATE)
						initializer("%S", "")
					}
				}
				
				for (dpComponent in DatapackComponentType.values()) {
					val componentFolder = namespace.resolve(dpComponent.folderName)
					if (componentFolder.exists() && componentFolder.isDirectory) {
						if (verbose) println("Adding ${dpComponent.name.lowercase()} in namespace $namespaceName")
						handleComponent(dpComponent, componentFolder, namespaceName)
					}
				}
				val functionParser = FunctionParser(namespace, parentPackage, null)
				functionParser(this, this@fileSpec)
			}
		}
		file.writeTo(outputDir)
	}

	/**
	 * Checks if the given name is a valid Kotlin identifier.
	 * Returns false if it starts with a number or contains invalid characters.
	 */
	private fun isValidKotlinIdentifier(name: String): Boolean {
		if (name.isEmpty()) return false
		if (name[0].isDigit()) return false
		return name.all { it.isLetterOrDigit() }
	}

	private fun TypeBuilder.handleComponent(
		componentType: DatapackComponentType,
		namespace: File,
		namespaceName: String,
		parentClassName: String = ""
	) {
		data class FolderContext(
			val folder: File,
			val parentPath: String,
			val parentClassName: String,
			val builder: TypeBuilder
		)

		val stack = mutableListOf<FolderContext>()
		stack.add(FolderContext(namespace, "", parentClassName, this))

		while (stack.isNotEmpty()) {
			val (currentFolder, parentPath, currentParentClassName, currentBuilder) = stack.removeAt(stack.lastIndex)

			for (componentOrSubFolder in currentFolder.listFiles() ?: emptyArray()) {
				if (componentOrSubFolder.isDirectory) {
					val subFolderName = componentOrSubFolder.name.substringAfterLast('/')
					val sanitizedSubFolderName = subFolderName.sanitizePascal()

					val newParentPath = if (parentPath.isEmpty()) subFolderName else "$parentPath/$subFolderName/"
					val hasParent = currentParentClassName.isNotEmpty()
					val newParentClassName = if (!hasParent)
						sanitizedSubFolderName else
						"$currentParentClassName.$sanitizedSubFolderName"

					val subObjectBuilder = currentBuilder.objectBuilder(sanitizedSubFolderName) {
						// Check if object name might not be a valid Kotlin identifier
						if (verbose) println("Adding sub-object for $newParentClassName in namespace $namespaceName which is valid : ${isValidKotlinIdentifier(sanitizedSubFolderName)}")
						if (!isValidKotlinIdentifier(sanitizedSubFolderName) &&
							!currentBuilder.typeSpecs.containsKey(sanitizedSubFolderName)
						) {
							addAnnotation<Suppress> {
								addMember("%S", "ClassName")
							}
						}

						if (!properties.containsKey("PATH")) {
							if (hasParent) {
								property<String>("PATH") {
									addModifiers(KModifier.CONST, KModifier.PRIVATE)
									initializer("%P", "\${${currentParentClassName.split(".").takeLast(2).joinToString(".")}.PATH}/$subFolderName/")
								}
							} else {
								property<String>("PATH") {
									addModifiers(KModifier.CONST, KModifier.PRIVATE)
									initializer("%P", subFolderName)
								}
							}
						}
					}
					stack.add(FolderContext(
						componentOrSubFolder,
						newParentPath,
						newParentClassName,
						subObjectBuilder
					))
				} else {
					val fileName = componentOrSubFolder.nameWithoutExtension
					if (componentOrSubFolder.extension != componentType.fileExtension) {
						if (verbose) println("Skipping $fileName because it's a ${componentOrSubFolder.extension} file instead of ${componentType.fileExtension}")
						continue
					}

					var sanitizedFileName = fileName.sanitizeCamel()
					val context = mapOf("namespace" to namespaceName, "name" to fileName)
					
					// Check if the sanitized name is a valid Kotlin identifier
					val needsPrefix = sanitizedFileName[0].isDigit()

					// For numeric filenames, add "n" prefix
					if (needsPrefix) {
						sanitizedFileName = "n$sanitizedFileName"
					}
					val needsSuppressAnnotation = !isValidKotlinIdentifier(sanitizedFileName)
					


					if (componentType.returnType != componentType.koreMethodOrClass) {
						if (currentBuilder.functions.containsKey(sanitizedFileName))
							sanitizedFileName = "${sanitizedFileName}${componentType.duplicateSuffix}"
						currentBuilder.function(sanitizedFileName) {
							if (needsSuppressAnnotation) {
								addAnnotation<Suppress> {
									addMember("%S", "FunctionName")
								}
							}
							
							if (needsPrefix) {
								addDocs(
									"This function was renamed to be a valid Kotlin identifier",
									"Minecraft will identify it as `$namespaceName:\${path to element}/$fileName`."
								)
							}
							
							if (componentType.requiredContext != null) {
								val contextParamName = componentType.requiredContext!!.simpleName.sanitizeCamel()
								this.contextParameter(contextParamName, componentType.requiredContext!!)
								returns(Command::class.asClassName())
								addStatement(
									"return $contextParamName.%T(%L)",
									componentType.koreMethodOrClass,
									handleComponentParameters(componentType.parameters, context)
								)
							} else {
								returns(Command::class.asClassName())
								addStatement(
									"return %T(%L)",
									componentType.koreMethodOrClass,
									handleComponentParameters(componentType.parameters, context)
								)
							}
						}
					} else {
						if (currentBuilder.properties.containsKey(sanitizedFileName))
							sanitizedFileName = "${sanitizedFileName}${componentType.duplicateSuffix}"
						currentBuilder.property(sanitizedFileName, componentType.returnType) {
							if (needsSuppressAnnotation) {
								addAnnotation<Suppress> {
									addMember("%S", "PropertyName")
								}
							}
							
							if (needsPrefix) {
								addDocs(
									"This function was renamed to be a valid Kotlin identifier.",
									"Minecraft will identify it as `$namespaceName:\${path to element}/$fileName`.",
								)
							}
							
							initializer(
								"%T(%L)",
								componentType.koreMethodOrClass,
								handleComponentParameters(componentType.parameters, context)
							)
						}
					}
				}
			}
		}
	}

	private fun handleComponentParameters(parameters: Map<ParameterSpec, Any?>, context: Map<String, String>): String {
		return parameters.map { (parameter, value) ->
			val parameterName = parameter.name
			if (value == null) {
				"$parameterName = ${when (parameterName) {
					in nameTypes -> "\"\${PATH}${context["name"]}\""
					"namespace" -> "namespace"
					else -> if (context.containsKey(parameterName)) "\"${context[parameterName]}\"" else throw IllegalArgumentException("Unknown base parameter name: $parameterName")
				}}"
			} else {
				"$parameterName = $value"
			}
		}.joinToString(", ")
	}
}

fun String.sanitizeCamel() = sanitizePascal().replaceFirstChar { if (!it.isLowerCase()) it.lowercase() else it.toString() }

fun String.sanitizePascal() = pascalCase()
	.replace('-', '_')
	.replace(".", "_")

private val nameTypes = setOf(
	"name", "damageType", "tagName", "paintingVariant", "biome", "structure",
	"worldPreset", "densityFunctionType", "feature", "instrument", "dimension",
	"preset"
)