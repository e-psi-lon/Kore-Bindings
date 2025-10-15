package io.github.e_psi_lon.kore.bindings.generation

import java.nio.file.FileSystems
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

fun main(args: Array<String>) {
	val startTime = System.currentTimeMillis()
	val arguments = args.toList()
    val verbose = arguments.contains("-v") || arguments.contains("--verbose")
    val quiet = arguments.contains("-q") || arguments.contains("--quiet")
    val logger = Logger(false, level = when {
        verbose -> Level.DEBUG
        quiet -> Level.ERROR
        else -> Level.INFO
    })

	if (arguments.contains("-h") || arguments.contains("--help")) {
		printHelp()
		return
	}
	val packageName = getArgValue(arguments, "-p", "--package")
		?: run {
            logger.error("Package name is required (-p, --package)")
			printHelp()
			return
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
		logger.error(" Either a datapack directory (-d) or zip file (-z) must be provided")
		printHelp()
		return
	}
	if (directoryPath != null && zipPath != null) {
		logger.error("Only one of datapack directory (-d) or zip file (-z) should be provided")
		printHelp()
		return
	}
	directoryPath?.let {
		if (!it.exists() || !it.isDirectory()) {
			logger.error("Datapack directory does not exist or is not a directory: ${it.absolutePathString()}")
			return
		}
	}
	zipPath?.let {
		if (!it.exists() || !it.isRegularFile()) {
			logger.error("Datapack zip file does not exist or is not a file: ${it.absolutePathString()}")
			return
		}
	}
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
                namespace = "",
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
	val seconds = time / 1000
	val minutes = seconds / 60
	val hours = minutes / 60
	val days = hours / 24
	return buildString {
		if (days > 0) append("${days}days ")
		if (hours > 0) append("${hours}h ")
		if (minutes > 0) append("${minutes}min ")
		if (seconds > 0) append("${seconds}s ")
		append("${time % 1000}ms")
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