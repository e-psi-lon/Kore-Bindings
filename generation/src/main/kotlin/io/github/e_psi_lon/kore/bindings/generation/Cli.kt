package io.github.e_psi_lon.kore.bindings.generation

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun main(args: Array<String>) {
	val startTime = System.currentTimeMillis()
	val arguments = args.toList()
    val verbose = getArgValue(arguments, "-v", "--verbose")?.toBoolean() ?: false
    val logger = Logger(false, level = if (verbose) Level.DEBUG else Level.INFO)

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
	var outputPath = getArgValue(arguments, "-o", "--output")
	val providedParentPackage = getArgValue(arguments, "-pp", "--parent-package")
	val parentPackage = providedParentPackage ?: packageName.substringBeforeLast(".", "")
	val originalOutputPath = outputPath ?: "generated"
    logger.info("Output path: $originalOutputPath")
	var bundled = false
	if (outputPath != null && outputPath.endsWith(".zip")) {
		bundled = true
		val tempDir = File.createTempFile("datapack", "beforeZip")
		tempDir.delete()
		tempDir.mkdirs()
		outputPath = tempDir.absolutePath
        logger.debug("Bundled mode enabled. Temporary directory created: $outputPath")
	}
	val outputDir = outputPath
		?.let { File(it) }
		?: File("generated")
	val folderPath = getArgValue(arguments, "-f", "--folder")?.let { File(it) }
	val zipPath = getArgValue(arguments, "-z", "--zip")?.let { File(it) }
	if (folderPath == null && zipPath == null) {
		logger.error(" Either a datapack folder (-f) or zip file (-z) must be provided")
		printHelp()
		return
	}
	if (folderPath != null && zipPath != null) {
		logger.error("Only one of datapack folder (-f) or zip file (-z) should be provided")
		printHelp()
		return
	}
	folderPath?.let {
		if (!it.exists() || !it.isDirectory) {
			logger.error("Datapack folder does not exist or is not a directory: ${it.absolutePath}")
			return
		}
	}
	zipPath?.let {
		if (!it.exists() || !it.isFile) {
			logger.error("Datapack zip file does not exist or is not a file: ${it.absolutePath}")
			return
		}
	}
	GenerateDatapackBindings(
		folder = folderPath,
		zipFile = zipPath,
		outputDir = outputDir,
		packageName = packageName,
		parentPackage = parentPackage,
		verbose = verbose,
        logger = logger
	)
	if (bundled) {
		val zipFile = File(originalOutputPath)
		if (zipFile.exists()) {
			logger.warn("Zip file already exists and will be overwritten: ${zipFile.absolutePath}")
		}
		zipFile.parentFile?.mkdirs()

		ZipOutputStream(zipFile.outputStream().buffered()).use { zipOutput ->
			outputDir.walkTopDown().forEach { file ->
				if (file.isFile) {
					// Calculer correctement le chemin relatif pour éviter d'inclure le dossier temporaire
					val relativePath = file.relativeTo(outputDir).path
					zipOutput.putNextEntry(ZipEntry(relativePath))
					file.inputStream().buffered().use { input ->
						input.copyTo(zipOutput)
					}
					zipOutput.closeEntry()
				}
			}
		}

		// Remove the output directory after zipping
		outputDir.deleteRecursively()
		logger.info("Bundled generated files into ${zipFile.absolutePath}")
	}
	logger.info("Bindings generated successfully in ${File(originalOutputPath).absolutePath} in ${parseTime(System.currentTimeMillis() - startTime)}")
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
          -f, --folder PATH				The path to the datapack folder
          -z, --zip PATH				The path to the datapack zip file
          -pp, --parent-package NAME	Parent package name (default: package name without the last part)
		  -v, --verbose					Enable verbose output (default: false)
        
        Note: Either -f or -z must be provided, but not both.
    """.trimIndent())
}