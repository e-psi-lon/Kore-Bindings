package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.commands.Command
import io.github.e_psi_lon.kore.bindings.generation.poet.TypeBuilder
import io.github.e_psi_lon.kore.bindings.generation.poet.addDocs
import io.github.e_psi_lon.kore.bindings.generation.poet.fileSpec
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories

/**
 * Generates Kotlin Kore DSL bindings for a Minecraft datapack.
 *
 * This class takes a directory or zip file containing a Minecraft datapack and generates Kotlin bindings
 * for all datapack's components. The generated bindings are written to the specified output directory.
 *
 * @param directory The directory containing the datapack. If null, the zipFile parameter must be provided.
 * @param zipFile The zip file containing the datapack. If null, the directory parameter must be provided.
 * @param outputDir The directory where the generated bindings will be written.
 * @param packageName The package name for the generated bindings.
 * @param parentPackage The parent package containing objects definitions for scoreboards and storages.
 * Defaults to the same as packageName.
 * @param verbose If true, prints additional information during processing.
 *
 * @throws IllegalArgumentException If neither directory nor zipFile is provided, or if both are provided.
 * Can also be thrown if the provided directory or zip file is invalid.
 *
 */
@OptIn(ExperimentalKotlinPoetApi::class)
class GenerateDatapackBindings(
	val directory: File? = null,
	val zipFile: File? = null,
	val outputDir: File,
	val packageName: String,
    val logger: Logger,
	private val parentPackage: String,
	private val verbose: Boolean = false
) {
	init {
		if (directory == null && zipFile == null) {
            logger.error("Either directory or zipFile must be provided")
			throw IllegalArgumentException("Either directory or zipFile must be provided")
		}

		if (directory != null && zipFile != null) {
            logger.error("Only one of directory or zipFile must be provided")
			throw IllegalArgumentException("Only one of directory or zipFile must be provided")
		}

		if (directory != null) {
			handleDatapack(directory)
		} else {
			val tempDirectory = File.createTempFile("datapack", "extract")
			tempDirectory.delete()
			tempDirectory.mkdirs()
			// Extract zip file
			ZipFile(zipFile!!).use { zip ->
				zip.entries().asSequence().forEach { entry ->
					val entryFile = File(tempDirectory, entry.name)
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
			handleDatapack(tempDirectory)
		}
	}

	private fun handleDatapack(directory: File) {
		val dataDir = directory.resolve("data")
		if (!directory.resolve("pack.mcmeta").exists() || !dataDir.exists() || !dataDir.isDirectory) {
            logger.error("Datapack directory is invalid: ${directory.absolutePath}")
            throw IllegalArgumentException("Invalid datapack directory: ${directory.absolutePath}")
        }
		val namespaceGroups = mutableMapOf<String, MutableList<String>>()

		for (namespace in dataDir.listFiles { file -> file.isDirectory }!!) {
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

				val namespaceDir = dataDir.resolve(fullNamespace)
				if (!namespaceDir.exists() || !namespaceDir.isDirectory) continue

				val suffixFile = fileSpec(packageName, fullCapitalized) {
					addAnnotation<Suppress> {
						addMember("%S", "unused")
						addMember("%S", "RedundantVisibilityModifier")
						addMember("%S", "UnusedReceiverParameter")
					}
					// Create main object for the namespace
					objectBuilder(fullCapitalized) {
						// Check if object name might not be a valid Kotlin identifier
						if (!fullCapitalized.isValidKotlinIdentifier()) {
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
						for (dpComponent in DatapackComponentType.entries) {
							val componentDir = namespaceDir.resolve(dpComponent.directoryName)
							if (componentDir.exists() && componentDir.isDirectory) {
								logger.debug("Adding ${dpComponent.name.lowercase()} in namespace $fullNamespace")
								handleComponent(dpComponent, componentDir, fullNamespace)
							}
						}
						val functionParser = FunctionParser(logger, namespaceDir, parentPackage, prefix)
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
				if (!capitalizedName.isValidKotlinIdentifier()) {
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

				for (dpComponent in DatapackComponentType.entries) {
					val componentDir = namespace.resolve(dpComponent.directoryName)
					if (componentDir.exists() && componentDir.isDirectory) {
						if (verbose) println("Adding ${dpComponent.name.lowercase()} in namespace $namespaceName")
						handleComponent(dpComponent, componentDir, namespaceName)
					}
				}
				val functionParser = FunctionParser(logger, namespace, parentPackage, null)
				functionParser(this, this@fileSpec)
			}
		}
		file.writeTo(outputDir)
	}

	private fun TypeBuilder.handleComponent(
        componentType: DatapackComponentType,
        namespace: File,
        namespaceName: String,
        parentClassName: String = ""
	) {
		data class DirectoryContext(
			val directory: File,
			val parentPath: String,
			val parentClassName: String,
			val builder: TypeBuilder
		)

		val stack = mutableListOf<DirectoryContext>()
		stack.add(DirectoryContext(namespace, "", parentClassName, this))

		while (stack.isNotEmpty()) {
			val (currentDirectory, parentPath, currentParentClassName, currentBuilder) = stack.removeAt(stack.lastIndex)

			for (componentOrSubDirectory in currentDirectory.listFiles() ?: emptyArray()) {
				if (componentOrSubDirectory.isDirectory) {
					val subDirectoryName = componentOrSubDirectory.name.substringAfterLast('/')
					val sanitizedSubDirectoryName = subDirectoryName.sanitizePascal()

					val newParentPath = if (parentPath.isEmpty()) subDirectoryName else "$parentPath/$subDirectoryName/"
					val hasParent = currentParentClassName.isNotEmpty()
					val newParentClassName = if (!hasParent)
						sanitizedSubDirectoryName else
						"$currentParentClassName.$sanitizedSubDirectoryName"

					val subObjectBuilder = currentBuilder.objectBuilder(sanitizedSubDirectoryName) {
						// Check if object name might not be a valid Kotlin identifier
						logger.debug("Adding sub-object for $newParentClassName in namespace $namespaceName which is valid : ${sanitizedSubDirectoryName.isValidKotlinIdentifier()}")
						if (!sanitizedSubDirectoryName.isValidKotlinIdentifier() &&
							!currentBuilder.typeSpecs.containsKey(sanitizedSubDirectoryName)
						) {
							addAnnotation<Suppress> {
								addMember("%S", "ClassName")
							}
						}

						if (!properties.containsKey("PATH")) {
							if (hasParent) {
								property<String>("PATH") {
									addModifiers(KModifier.CONST, KModifier.PRIVATE)
									initializer("%P",
                                        $$"${$${
                                            currentParentClassName.split(".").takeLast(2).joinToString(".")
                                        }.PATH}/$$subDirectoryName/"
                                    )
								}
							} else {
								property<String>("PATH") {
									addModifiers(KModifier.CONST, KModifier.PRIVATE)
									initializer("%P", subDirectoryName)
								}
							}
						}
					}
					stack.add(DirectoryContext(
						componentOrSubDirectory,
						newParentPath,
						newParentClassName,
						subObjectBuilder
					))
				} else {
					val fileName = componentOrSubDirectory.nameWithoutExtension
					if (componentOrSubDirectory.extension != componentType.fileExtension) {
						logger.debug("Skipping $fileName because it's a ${componentOrSubDirectory.extension} file instead of ${componentType.fileExtension}")
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
					val needsSuppressAnnotation = !sanitizedFileName.isValidKotlinIdentifier()

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
                                    $$"Minecraft will identify it as `$$namespaceName:${path to element}/$$fileName`.",
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
					in nameTypes -> $$"\"${PATH}$${context["name"]}\""
					"namespace" -> "namespace"
					else -> if (context.containsKey(parameterName)) "\"${context[parameterName]}\"" else throw IllegalArgumentException("Unknown base parameter name: $parameterName")
				}}"
			} else {
				"$parameterName = $value"
			}
		}.joinToString(", ")
	}
}

private val nameTypes = setOf(
    "name", "damageType", "tagName", "paintingVariant", "biome", "structure",
    "worldPreset", "densityFunctionType", "feature", "instrument", "dimension",
    "preset"
)

@OptIn(ExperimentalPathApi::class)
private fun getZipFs(zipPath: Path) = FileSystems.newFileSystem(zipPath, emptyMap<String, Any>())

fun generateDatapackBinding(
    datapackSource: Path,
    isZip: Boolean,
    outputDir: Path,
    packageName: String,
    parentPackage: String,
    prefix: String?,
    logger: Logger,
) {
    outputDir.createDirectories()
    val fileSystem = if (isZip) getZipFs(datapackSource) else null
    fileSystem.use {
        val datapackDir = fileSystem?.getPath("/") ?: datapackSource
        val parser = DatapackParser(datapackDir, logger)
        val datapack = parser()
        val generator = BindingGenerator(logger, datapack, outputDir, packageName, parentPackage, prefix)
        generator()
    }
}

