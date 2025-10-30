package io.github.e_psi_lon.kore.bindings.generation

import java.nio.file.FileSystems
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	val startTime = System.currentTimeMillis()
	val arguments = args.toList()
    val verbose = arguments.any { it == "-v" || it == "--verbose" }
    val quiet = arguments.any { it == "-q" || it == "--quiet" }
    if (verbose && quiet) {
        System.err.println(Logger.format("Cannot use both verbose and quiet flags simultaneously.", Level.ERROR))
        exitProcess(1)
    }
    val logger = Logger.println(when {
        verbose -> Level.DEBUG
        quiet -> Level.ERROR
        else -> Level.INFO
    })
	if (arguments.any { it == "-h" || it == "--help" }) {
		printHelp()
        return
	}
	val packageName = getArgValue(arguments, "-p", "--package") ?: run {
        logger.error("Package name is required (-p, --package)")
        printHelp()
        exitProcess(1)
    }
	val providedParentPackage = getArgValue(arguments, "-pp", "--parent-package")
	val parentPackage = providedParentPackage ?: packageName.substringBeforeLast(".", "")
    val outputPath = getArgValue(arguments, "-o", "--output")
    val originalOutputPath = outputPath ?: "generated"
    logger.info("Output path: $originalOutputPath")
    val bundled = outputPath != null && outputPath.endsWith(".zip")
    val outputDir = outputPath
        ?.let { Path(it) }
        ?: Path("generated")
	val directoryPath = getArgValue(arguments, "-d", "--dir")?.let { Path(it) }
	val zipPath = getArgValue(arguments, "-z", "--zip")?.let { Path(it) }
	if (directoryPath == null && zipPath == null) {
		logger.error("Either a datapack directory (-d) or zip file (-z) must be provided")
		printHelp()
		exitProcess(1)
	}
	if (directoryPath != null && zipPath != null) {
		logger.error("Only one of datapack directory (-d) or zip file (-z) should be provided")
		printHelp()
        exitProcess(1)
	}
	directoryPath?.let {
		if (!it.exists() || !it.isDirectory()) {
			logger.error("Datapack directory does not exist or is not a directory: ${it.absolutePathString()}")
			exitProcess(1)
		}
	}
	zipPath?.let {
		if (!it.exists() || !it.isRegularFile()) {
			logger.error("Datapack zip file does not exist or is not a file: ${it.absolutePathString()}")
			exitProcess(1)
		}
	}
    // Temporary. To remove once the refactor is complete
    val useRefactor = arguments.contains("-r") || arguments.contains("--refactor")
    val fileSystem = if (bundled)
        FileSystems.newFileSystem(outputDir, emptyMap<String, Any>())
    else
        null
    fileSystem.use { fs ->
        val finalPath = fs?.getPath("/") ?: outputDir
        if (useRefactor) {
            generateDatapackBinding(
                datapackSource = zipPath ?: directoryPath!!,
                isZip = zipPath != null,
                outputDir = finalPath,
                packageName = packageName,
                parentPackage = parentPackage,
                prefix = null,
                logger = logger
            )
        } else
        GenerateDatapackBindings(
            directory = directoryPath?.toFile(),
            zipFile = zipPath?.toFile(),
            outputDir = finalPath.toFile(),
            packageName = packageName,
            parentPackage = parentPackage,
            verbose = verbose,
            logger = logger
        )
    }

	logger.info("Bindings generated successfully in ${Path(originalOutputPath).absolutePathString()} in ${parseTime(System.currentTimeMillis() - startTime)}")
}

private fun getArgValue(args: List<String>, shortFlag: String, longFlag: String): String? {
	val shortIndex = args.indexOf(shortFlag)
	val longIndex = args.indexOf(longFlag)

	return when {
		shortIndex >= 0 && shortIndex < args.size - 1 -> args[shortIndex + 1]
		longIndex >= 0 && longIndex < args.size - 1 -> args[longIndex + 1]
		else -> null
	}
}

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

private fun printHelp() {
	println("""
        Datapack Bindings Generator
        
        Usage:
          java -jar kore-bindings-generator.jar [options]
        
        Options:
          -h, --help					Display this help message
          -p, --package NAME			The package name for the generated bindings (required)
          -o, --output PATH				Output directory or zip file name for generated bindings (default: "generated")
          -d, --dir PATH				The path to the datapack directory
          -z, --zip PATH				The path to the datapack zip file
          -pp, --parent-package NAME	Parent package name (default: package name without the last part)
          -q, --quiet					Minimize output (default: false)
          -v, --verbose					Enable verbose output (default: false)
        
        Note: Either -d or -z must be provided, but not both.
    """.trimIndent())
}