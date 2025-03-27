package io.github.e_psi_lon.kore.bindings.generation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import java.io.File

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
 * 			datapackFolder.set(file("path/to/datapack"))
 * 			packageName.set("com.example.mypackage")
 *  }
 *  ```
 */
class BindingGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("bindings", BindingExtension::class.java, project)
		val outputDir = project.layout.buildDirectory.dir("generated/kore-bindings/main/kotlin/")
		val sourceSet = project.extensions.getByType<SourceSetContainer>().named("main")
		val generateTask = project.tasks.register("generateBindings") {
			group = "datapack"
			description = "Generate Kotlin bindings for Minecraft datapacks."
			doLast {
				val datapackFolder = extension.datapackFolder.orNull
					?: project.layout.projectDirectory.dir("src/main/resources/datapacks")
				if (!datapackFolder.asFile.exists()) {
					project.logger.error("Datapack folder does not exist: ${datapackFolder.asFile.absolutePath}")
					return@doLast
				}
				for (datapack in datapackFolder.asFile.listFiles()!!) {
					project.logger.lifecycle("Generating ${datapack.name}...")
					val packageName = extension.packageName.orNull ?: project.group.toString()
					val sanitizedPackageName = packageName.sanitizePackageName()

					if (datapack.isDirectory || datapack.extension == "zip") {
						outputDir.get().asFile.mkdirs()

						GenerateDatapackBindings(
							folder = if (datapack.isDirectory) datapack else null,
							zipFile = if (!datapack.isDirectory && datapack.extension == "zip") datapack else null,
							outputDir = outputDir.get().asFile,
							packageName = sanitizedPackageName,
						)
					} else {
						project.logger.warn("Unsupported file type: ${datapack.name}. Only directories and zip files are supported.")
					}
				}
			}
		}
		project.tasks.getByName("compileKotlin").dependsOn(generateTask)
		/*if (extension.configureSourceSet.get()) {
			sourceSet {

				java {
					srcDir(outputDir)
				}

				output.dir(
					mapOf("builtBy" to generateTask),
					outputDir,
				)
			}
		}*/
	}

}

/**
 * Extension for the [BindingGradlePlugin] to configure the plugin.
 *
 * @property project The Gradle project.
 * @property datapackFolder The folder containing the datapack.
 * @property packageName The package name for the generated bindings.
 */
open class BindingExtension(private val project: Project) {
	val datapackFolder: DirectoryProperty = project.objects.directoryProperty()
	val packageName: Property<String> = project.objects.property(String::class.java)
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