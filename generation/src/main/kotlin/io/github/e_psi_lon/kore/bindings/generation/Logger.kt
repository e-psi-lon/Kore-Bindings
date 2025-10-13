package io.github.e_psi_lon.kore.bindings.generation


import org.gradle.api.logging.Logger as GradleLogger


enum class Level {
    TRACE, DEBUG, INFO, WARN, ERROR
}

/**
 * Minimal abstraction allowing to switch between Gradle's logger and stdout/stderr
 */
class Logger(private val useGradleLogger: Boolean, private val gradleLogger: GradleLogger? = null, val level: Level = Level.INFO) {

    init {
        require(!useGradleLogger || gradleLogger != null) { "Gradle logger must be provided if useGradleLogger is true" }
    }
    fun info(message: String) {
        if (level > Level.INFO) return
        if (useGradleLogger && gradleLogger != null) gradleLogger.lifecycle(message)
        else println("[INFO] $message")
    }

    fun warn(message: String) {
        if (level > Level.WARN) return
        if (useGradleLogger && gradleLogger != null) gradleLogger.warn(message)
        else println("[WARN] $message")
    }

    fun error(message: String) {
        if (level > Level.ERROR) return
        if (useGradleLogger && gradleLogger != null) gradleLogger.error(message)
        else System.err.println("[ERROR] $message")
    }

    fun <T: Exception> error(message: String, exception: T): T {
        if (level > Level.ERROR) return exception
        if (useGradleLogger && gradleLogger != null) gradleLogger.error(message, exception)
        else {
            System.err.println("[ERROR] $message")
            exception.printStackTrace(System.err)
        }
        return exception
    }

    fun debug(message: String) {
        if (level > Level.DEBUG) return
        if (useGradleLogger && gradleLogger != null) gradleLogger.debug(message)
        else println("[DEBUG] $message")
    }

    fun trace(message: String) {
        if (level > Level.TRACE) return
        if (useGradleLogger && gradleLogger != null) gradleLogger.trace(message)
        else println("[TRACE] $message")
    }
}