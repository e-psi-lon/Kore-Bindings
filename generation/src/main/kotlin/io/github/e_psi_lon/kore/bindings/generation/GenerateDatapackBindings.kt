package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.generated.Gamerules.Companion.camelCase
import io.github.ayfri.kore.utils.pascalCase
import java.io.File
import java.util.zip.ZipFile

@OptIn(ExperimentalKotlinPoetApi::class)
class GenerateDatapackBindings(
	val folder: File? = null,
	val zipFile: File? = null,
	val outputDir: File,
	val packageName: String,
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
		// On vérifie le pack.mcmeta et le dossier data
		if (!folder.resolve("pack.mcmeta").exists() || !dataFolder.exists() || !dataFolder.isDirectory)
			throw IllegalArgumentException("Invalid datapack folder: ${folder.absolutePath}")
		// On regroupe les namespaces par préfixe
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

		// Traitement des namespaces groupés
		for ((prefix, namespaces) in namespaceGroups) {
			val capitalizedPrefix = prefix.sanitizePascal()
			val prefixFile = fileSpec(packageName, capitalizedPrefix) {
				objectBuilder(capitalizedPrefix) {
					addProperty(PropertySpec.builder("namespace", String::class)
						.initializer("%S", prefix)
						.build())
				}
			}
			prefixFile.writeTo(outputDir)

			// Création des fichiers pour chaque sous namespace
			for (fullNamespace in namespaces) {
				val namespaceSuffix = fullNamespace.substringAfter('.')
				val capitalizedSuffix = namespaceSuffix.sanitizePascal()
				val fullCapitalized = "${capitalizedPrefix}${capitalizedSuffix}"

				// Recherche du dossier de namespace
				val namespaceFolder = dataFolder.resolve(fullNamespace)
				if (!namespaceFolder.exists() || !namespaceFolder.isDirectory) continue

				// Création du fichier pour le sous namespace
				val suffixFile = fileSpec(packageName, fullCapitalized) {
					// Créer l'objet principal du sous namespace
					objectBuilder(fullCapitalized) {
						addProperty(PropertySpec.builder("namespace", String::class)
							.initializer("%S", fullNamespace)
							.build())

						// Traiter tous les composants
						for (dpComponent in DatapackComponentType.values()) {
							val componentFolder = namespaceFolder.resolve(dpComponent.folderName)
							if (componentFolder.exists() && componentFolder.isDirectory) {
								println("Adding ${dpComponent.name.lowercase()} in namespace $fullNamespace")
								handleComponent(dpComponent, componentFolder, fullNamespace, this)
							}
						}
					}

					// Ajouter l'extension au fichier principal
					property(namespaceSuffix.sanitizeCamel(), ClassName(packageName, fullCapitalized)) {
						receiver(ClassName(packageName, capitalizedPrefix))
						getter {
							addStatement("return %T", ClassName(packageName, fullCapitalized))
						}
						build()
					}
				}
				suffixFile.writeTo(outputDir)
			}
		}
	}

	private fun processNamespace(namespace: File, namespaceName: String) {
		val capitalizedName = namespaceName.sanitizePascal()
		val file = fileSpec(packageName, capitalizedName) {
			objectBuilder(capitalizedName) {
				addProperty(PropertySpec.builder("namespace", String::class, KModifier.PUBLIC)
					.initializer("%S", namespaceName)
					.build())

				for (dpComponent in DatapackComponentType.values()) {
					val componentFolder = namespace.resolve(dpComponent.folderName)
					if (componentFolder.exists() && componentFolder.isDirectory) {
						println("Adding ${dpComponent.name.lowercase()} in namespace $namespaceName")
						handleComponent(dpComponent, componentFolder, namespaceName, this)
					}
				}
			}
		}
		file.writeTo(outputDir)
	}



	private fun TypeSpec.Builder.handleComponent(componentType: DatapackComponentType, namespace: File, namespaceName: String, mainObject: TypeSpec.Builder, parentClassName: String = "") {
		// Recursively go through the functions in the namespace folder (data/$namespace/function)
		// Knowing that the first folder won't have a parent "path", only a namespace
		for (componentOrSubFolder in namespace.listFiles()!!) {
			if (componentOrSubFolder.isDirectory) {
				// Create a new sub-object and call handleFunctions on it
				val subFolderName = componentOrSubFolder.name.substringAfterLast('/')
				val sanitizedSubFolderName = subFolderName.sanitizePascal()
				objectBuilder(sanitizedSubFolderName) {
					val hasParent = parentClassName.isNotEmpty()
					if (hasParent) {
						property<String>("path") {
							initializer("%P", '$'+"{$parentClassName.path}/$subFolderName")
						}
					} else {
						property<String>("path") {
							initializer("%P", '$'+"{${namespaceName.sanitizePascal()}.namespace}:$subFolderName")
						}
					}
					handleComponent(componentType, componentOrSubFolder, namespaceName, mainObject, if (hasParent) "$parentClassName.$sanitizedSubFolderName" else sanitizedSubFolderName)
				}
			} else {
				// get the file name
				val fileName = componentOrSubFolder.nameWithoutExtension
				// Ensure the file extension is correct
				if (componentOrSubFolder.extension != componentType.fileExtension) {
					println("Skipping $fileName because it's a ${componentOrSubFolder.extension} file instead of ${componentType.fileExtension}")
					continue
				}
				val sanitizedFileName = fileName.sanitizeCamel()
				val context = mapOf("namespace" to namespaceName, "name" to fileName)
				if (componentType.returnType != componentType.koreMethodOrClass)
					function(sanitizedFileName) {
						if (componentType.requiredContext != null) {
							contextReceivers(componentType.requiredContext!!)
						}
						returns(Command::class.asClassName())
						addStatement(
							"return %T(%L)",
							componentType.koreMethodOrClass,
							handleComponentParameters(componentType.parameters, context)
						)
					}
				else
					property(sanitizedFileName, componentType.returnType) {
						initializer(
							"%T(%L)",
							componentType.koreMethodOrClass,
							handleComponentParameters(componentType.parameters, context)
						)
					}
			}
		}
	}

	private fun handleComponentParameters(parameters: Map<ParameterSpec, Any?>, context: Map<String, String>): String {
		return parameters.map { (parameter, value) ->
			val parameterName = parameter.name
			if (value == null) {
				"$parameterName = ${when (parameterName) {
					"name" -> "\"${context["name"]}\""
					"namespace" -> "${context["namespace"]?.sanitizePascal()}.namespace"
					else -> if (context.containsKey(parameterName)) "\"${context[parameterName]}\"" else throw IllegalArgumentException("Unknown base parameter name: $parameterName")
				}}"
			} else {
				"$parameterName = $value"
			}
		}.joinToString(", ")
	}
}

fun String.sanitizeCamel() = camelCase()
	.replace('-', '_')
	.replace(".", "_")

fun String.sanitizePascal() = pascalCase()
	.replace('-', '_')
	.replace(".", "_")