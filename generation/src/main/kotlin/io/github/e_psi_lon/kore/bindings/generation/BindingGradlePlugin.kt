package io.github.e_psi_lon.kore.bindings.generation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.api.logging.Logger as GradleLogger

/**
 * Gradle plugin to generate Kotlin bindings for Minecraft datapacks.
 *
 * This plugin allows you to generate Kotlin bindings for Minecraft datapacks, making it easier to work with
 * Minecraft commands and functions in a type-safe manner.
 *
 * @see [GenerateDatapackBindings]
 *
 * @sample
 * ```kotlin
 * plugins {
 * 	id("io.github.e_psi_lon.kore.bindings.generation")
 * 	}
 *
 * 	bindings {
 * 			datapackDirectory.set(file("path/to/datapack"))
 * 			packageName.set("com.example.mypackage")
 *  }
 *  ```
 */
class BindingGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val logger = Logger(true, project.logger, project.logger.logLevel)
		val extension = project.extensions.create("bindings", BindingExtension::class.java, project)
		val outputDir = project.layout.buildDirectory.dir("generated/kore-bindings/main/kotlin/")
		val sourceSet = project.extensions.getByType<SourceSetContainer>().named("main")
		val generateTask = project.tasks.register("generateBindings") {
			group = "datapack"
			description = "Generate Kotlin bindings for Minecraft datapacks."
			doLast {
				val datapackDir = extension.datapackDir.orNull
					?: project.layout.projectDirectory.dir("src/main/resources/datapacks")
				if (!datapackDir.asFile.exists()) {
					logger.error("Datapack directory does not exist: ${datapackDir.asFile.absolutePath}")
					return@doLast
				}
				outputDir.get().asFile.mkdirs()
				for (datapack in datapackDir.asFile.listFiles()!!) {
					logger.info("Generating ${datapack.name}...")
					val packageName = extension.packageName.orNull ?: project.group.toString()
					val sanitizedPackageName = packageName.sanitizePackageName()

					if (datapack.isDirectory || datapack.extension == "zip") {

						GenerateDatapackBindings(
							directory = if (datapack.isDirectory) datapack else null,
							zipFile = if (!datapack.isDirectory && datapack.extension == "zip") datapack else null,
							outputDir = outputDir.get().asFile,
							packageName = sanitizedPackageName,
							parentPackage = extension.parentPackage.get().sanitizePackageName(),
							logger = logger
						)
					} else {
						logger.warn("Unsupported file type: ${datapack.name}. Only directories and zip files are supported.")
					}
				}
			}
		}
		project.tasks.getByName("compileKotlin").dependsOn(generateTask)
		if (extension.configureSourceSet.get()) {
			sourceSet.configure {
				java.srcDir(outputDir)

				// Configurer la sortie pour inclure les fichiers générés
				output.dir(
					mapOf("builtBy" to generateTask),
					outputDir
				)
			}
		}
	}

    private val GradleLogger.logLevel: Level
        get() = when {
            isTraceEnabled -> Level.TRACE
            isDebugEnabled -> Level.DEBUG
            isInfoEnabled -> Level.INFO
            isWarnEnabled -> Level.WARN
            isErrorEnabled -> Level.ERROR
            else -> Level.INFO
        }
}

/**
 * Extension for the [BindingGradlePlugin] to configure the plugin.
 *
 * @property project The Gradle project.
 * @property datapackDir The directory containing the datapack.
 * @property parentPackage The parent package for the generated bindings. Defaults to the package name without the last segment.
 * @property packageName The package name for the generated bindings.
 */
open class BindingExtension(private val project: Project) {
	val datapackDir: DirectoryProperty = project.objects.directoryProperty()
	val packageName: Property<String> = project.objects.property(String::class.java)
	val parentPackage = project.objects.property(String::class.java)
		.convention(project.provider { packageName.get().substringBeforeLast('.') })
		.apply {
			finalizeValueOnRead()
		}
	val configureSourceSet: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
}


private fun String.sanitizePackageName() =
	this.replace('-', '_')
		.replace(' ', '_')
		.replace("'", "")
		.replace("!", "")
		.replace("@", "")
		.replace("#", "")
		.replace("$", "")
		.replace("%", "")
		.replace("^", "")
		.replace("&", "")
		.replace("*", "")