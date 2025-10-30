package io.github.e_psi_lon.kore.bindings.generation


import com.github.ajalt.clikt.core.CliktCommand
import org.gradle.api.logging.Logger as GradleLogger


enum class Level: Comparable<Level> {
    TRACE, DEBUG, INFO, WARN, ERROR
}

private interface ILogger {
    fun log(message: String, level: Level)
}

private class GradleLoggerWrapper(private val logger: GradleLogger): ILogger {
    override fun log(message: String, level: Level) {
        when (level) {
            Level.TRACE -> logger.trace(message)
            Level.DEBUG -> logger.debug(message)
            Level.INFO -> logger.info(message)
            Level.WARN -> logger.warn(message)
            Level.ERROR -> logger.error(message)
        }
    }
}

private class PrintlnLogger: ILogger {
    override fun log(message: String, level: Level) {
        val output = if (level == Level.ERROR) System.err else System.out
        output.println(Logger.format(message, level))
    }
}

private class EchoLogger(private val context: CliktCommand): ILogger {
    override fun log(message: String, level: Level) {
        context.echo(Logger.format(message, level), err = level == Level.ERROR)
    }
}

/**
 * Abstraction layer for multiple logging implementations.
 */
class Logger private constructor(private val logger: ILogger, private val level: Level) {

    fun log(message: String, level: Level) {
        if (!shouldLog(level)) return
        logger.log(message, level)
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


    companion object {
        fun format(message: String, level: Level): String {
            return "[${level.name}] $message"

        }

        fun gradle(logger: GradleLogger, level: Level = Level.INFO): Logger = Logger(GradleLoggerWrapper(logger), level)
        fun echo(context: CliktCommand, level: Level = Level.INFO): Logger = Logger(EchoLogger(context), level)
        fun println(level: Level = Level.INFO): Logger = Logger(PrintlnLogger(), level)
    }
}