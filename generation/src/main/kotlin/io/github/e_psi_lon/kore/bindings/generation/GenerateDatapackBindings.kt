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
						property<String>("namespace") {
							initializer("%S", fullNamespace)
						}
						// Traiter tous les composants
						for (dpComponent in DatapackComponentType.values()) {
							val componentFolder = namespaceFolder.resolve(dpComponent.folderName)
							if (componentFolder.exists() && componentFolder.isDirectory) {
								println("Adding ${dpComponent.name.lowercase()} in namespace $fullNamespace")
								handleComponent(dpComponent, componentFolder, fullNamespace, this)
							}
						}
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
				property<String>("namespace") {
					initializer("%S", namespaceName)
				}
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



	private fun TypeBuilder.handleComponent(componentType: DatapackComponentType, namespace: File, namespaceName: String, mainObject: TypeBuilder, parentClassName: String = "") {
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
							initializer("%P", "\$namespace:$subFolderName")
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
				var sanitizedFileName = fileName.sanitizeCamel()
				val context = mapOf("namespace" to namespaceName, "name" to fileName)
				if (componentType.returnType != componentType.koreMethodOrClass) {
					if (functions.containsKey(sanitizedFileName))
						sanitizedFileName = "${sanitizedFileName}${componentType.duplicateSuffix}"
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
				} else {
					if (properties.containsKey(sanitizedFileName))
						sanitizedFileName = "${sanitizedFileName}${componentType.duplicateSuffix}"
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
	}

	private fun handleComponentParameters(parameters: Map<ParameterSpec, Any?>, context: Map<String, String>): String {
		return parameters.map { (parameter, value) ->
			val parameterName = parameter.name
			if (value == null) {
				"$parameterName = ${when (parameterName) {
					"name", "tagName" -> "\"${context["name"]}\""
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