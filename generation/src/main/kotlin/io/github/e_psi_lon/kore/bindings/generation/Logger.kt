package io.github.e_psi_lon.kore.bindings.generation


import org.gradle.api.logging.Logger as GradleLogger


enum class Level: Comparable<Level> {
    TRACE, DEBUG, INFO, WARN, ERROR
}

/**
 * Minimal abstraction allowing to switch between Gradle's logger and stdout/stderr
 */
class Logger(private val useGradleLogger: Boolean, private val gradleLogger: GradleLogger? = null, val level: Level = Level.INFO) {
    init {
        require(!useGradleLogger || gradleLogger != null) { "Gradle logger must be provided if useGradleLogger is true" }
    }

    fun log(message: String, level: Level) {
        if (!shouldLog(level)) return
        if (useGradleLogger && gradleLogger != null) {
            when (level) {
                Level.TRACE -> gradleLogger.trace(message)
                Level.DEBUG -> gradleLogger.debug(message)
                Level.INFO -> gradleLogger.lifecycle(message)
                Level.WARN -> gradleLogger.warn(message)
                Level.ERROR -> gradleLogger.error(message)
            }
        } else {
            val output = if (level == Level.ERROR) System.err else System.out
            output.println("[${level.name}] $message")
        }
    }
    fun shouldLog(level: Level): Boolean = level >= this.level

    fun info(message: String) = log(message, Level.INFO)
    fun warn(message: String) = log(message, Level.WARN)
    fun error(message: String) = log(message, Level.ERROR)
    fun debug(message: String) = log(message, Level.DEBUG)
    fun trace(message: String) = log(message, Level.TRACE)

    fun <T: Exception> error(message: String, exception: T): T {
        if (shouldLog(Level.ERROR))
            log(message + "\n" + exception.stackTraceToString(), Level.ERROR)
        return exception
    }
}