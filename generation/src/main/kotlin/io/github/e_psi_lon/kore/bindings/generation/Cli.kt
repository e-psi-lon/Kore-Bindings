package io.github.e_psi_lon.kore.bindings.generation

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.default
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.time.measureTime

sealed class DatapackSource {
    data class Directory(val path: Path) : DatapackSource()
    data class Zip(val path: Path) : DatapackSource()
}

class GenerateBindings : CliktCommand(name = "java -jar kore-bindings-generator.jar") {
    private val packageName by option("-p", "--package", help = "The package name for the generated bindings (required)")
        .required()

    private val parentPackage by option("-pp", "--parent-package", help = "Parent package name (default: package name without the last part)")
    
    private val outputPath by option("-o", "--output", help = "Output directory or zip file name for generated bindings (default: \"generated\")",
        completionCandidates = CompletionCandidates.Path
    ).default("generated")

    private val datapackSource by mutuallyExclusiveOptions(
        option("-d", "--dir", help = "The path to the datapack directory", completionCandidates = CompletionCandidates.Path)
            .convert {
                val path = Path(it)
                if (!path.exists() || !path.isDirectory()) {
                    fail("Datapack directory does not exist or is not a directory: ${path.absolutePathString()}")
                }
                DatapackSource.Directory(path)
            },
        option("-z", "--zip", help = "The path to the datapack zip file", completionCandidates = CompletionCandidates.Path)
            .convert {
                val path = Path(it)
                if (!path.exists() || !path.isRegularFile()) {
                    fail("Datapack zip file does not exist or is not a file: ${path.absolutePathString()}")
                }
                DatapackSource.Zip(path)
            }
    ).single().required()

    private val level by mutuallyExclusiveOptions(
        option("-v", "--verbose", help = "Enable verbose output").flag().convert { Level.DEBUG },
        option("-q", "--quiet", help = "Minimize output").flag().convert { Level.ERROR }
    ).default(Level.INFO)

    // Temporary. To remove once the refactor is done
    private val useRefactor by option("-r", "--refactor", help = "Use refactored generation system (temporary)").flag()

    override fun run() {
        // Create logger
        val logger = Logger.echo(this, level)

        val originalOutputPath: String
        val startTime = measureTime {
            val finalParentPackage = parentPackage ?: packageName.substringBeforeLast(".", "")
            originalOutputPath = outputPath
            val outputPathValue = outputPath
            val bundled = outputPathValue.endsWith(".zip")
            val outputDir = Path(outputPathValue)

            logger.info("Output path: $originalOutputPath")

            val fileSystem = if (bundled)
                FileSystems.newFileSystem(outputDir, emptyMap<String, Any>())
            else
                null

            fileSystem.use { fs ->
                val finalPath = fs?.getPath("/") ?: outputDir

                if (useRefactor) {
                    val source = when (val dpSource = datapackSource) {
                        is DatapackSource.Directory -> dpSource.path
                        is DatapackSource.Zip -> dpSource.path
                    }
                    generateDatapackBinding(
                        datapackSource = source,
                        isZip = datapackSource is DatapackSource.Zip,
                        outputDir = finalPath,
                        packageName = packageName,
                        parentPackage = finalParentPackage,
                        prefix = null,
                        logger = logger
                    )
                } else {
                    val dir = (datapackSource as? DatapackSource.Directory)?.path?.toFile()
                    val zip = (datapackSource as? DatapackSource.Zip)?.path?.toFile()
                    GenerateDatapackBindings(
                        directory = dir,
                        zipFile = zip,
                        outputDir = finalPath.toFile(),
                        packageName = packageName,
                        parentPackage = finalParentPackage,
                        verbose = level <= Level.DEBUG,
                        logger = logger
                    )
                }
            }
        }


        echo("Bindings generated successfully in ${Path(originalOutputPath).absolutePathString()} in ${parseTime(startTime.inWholeMilliseconds)}")
    }
}

fun main(args: Array<String>) = GenerateBindings().main(args)

private fun parseTime(time: Long): String {
    val hours = (time / (60 * 60 * 1000)) % 24
    val minutes = (time / (60 * 1000)) % 60
    val seconds = (time / 1000) % 60
    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min ")
        if (seconds > 0) append("$seconds s ")
        append("${time % 1000} ms")
    }
}

