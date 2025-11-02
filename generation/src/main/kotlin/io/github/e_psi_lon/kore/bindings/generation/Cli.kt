package io.github.e_psi_lon.kore.bindings.generation

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.*
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.time.Duration
import kotlin.time.measureTime

sealed class DatapackSource {
    data class Directory(val path: Path) : DatapackSource()
    data class Zip(val path: Path) : DatapackSource()
}

class GenerateBindings : CliktCommand(name = "java -jar kore-bindings-generator.jar") {
    private val packageName by option("-p", "--package", help = "The package name for the generated bindings (required)")
        .required()
        .validate {
            if (it.isBlank())
                fail("Package name cannot be blank")
            else if (!it.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$")))
                fail("Package name must be a valid Java package name")
        }

    private val parentPackage by option("-pp", "--parent-package", help = "Parent package name (default: package name without the last part)")
        .defaultLazy { packageName.substringBeforeLast(".", "") }
    
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
        option("-v", "--verbose", help = "Enable verbose output").flag().convert { LogLevel.DEBUG },
        option("-q", "--quiet", help = "Minimize output").flag().convert { LogLevel.ERROR }
    ).default(LogLevel.INFO)

    // Temporary. To remove once the refactor is done
    private val useRefactor by option("-r", "--refactor", help = "Use refactored generation system (temporary)").flag()

    override fun run() {
        // Create logger
        val logger = Logger.echo(this, level)

        val startTime = measureTime {
            val bundled = outputPath.endsWith(".zip")
            val outputDir = Path(outputPath)

            logger.info("Output path: $outputPath")

            val fileSystem = if (bundled)
                FileSystems.newFileSystem(outputDir, emptyMap<String, Any>())
            else
                null

            fileSystem.use { fs ->
                val finalPath = fs?.getPath("/") ?: outputDir

                if (useRefactor) {
                    val (source, isZip) = when (val dpSource = datapackSource) {
                        is DatapackSource.Directory -> dpSource.path to false
                        is DatapackSource.Zip -> dpSource.path to true
                    }
                    generateDatapackBinding(
                        datapackSource = source,
                        isZip = isZip,
                        outputDir = finalPath,
                        packageName = packageName,
                        parentPackage = parentPackage,
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
                        parentPackage = parentPackage,
                        verbose = level <= LogLevel.DEBUG,
                        logger = logger
                    )
                }
            }
        }


        echo("Bindings generated successfully in ${Path(outputPath).absolutePathString()} in ${formatDuration(startTime)}")
    }


    private fun formatDuration(duration: Duration) = duration.toComponents { hours, minutes, seconds, nanoseconds ->
        listOfNotNull(
            hours.takeIf { it > 0 }?.let { "$it h" },
            minutes.takeIf { it > 0 }?.let { "$it min" },
            seconds.takeIf { it > 0 }?.let { "$it s" },
            nanoseconds.takeIf { it > 0 }?.let { "${it / 1_000_000} ms" }
        ).joinToString(" ").ifEmpty { "0 ms" }
    }
}

fun main(args: Array<String>) = GenerateBindings().main(args)