package io.github.e_psi_lon.kore.bindings.generation


import com.github.ajalt.clikt.core.CliktCommand
import org.gradle.api.logging.Logger as GradleLogger


enum class LogLevel {
    // The order is used for comparison of log level
    TRACE, DEBUG, INFO, WARN, ERROR
}

private interface LoggerImpl {
    fun log(message: String, level: LogLevel)
    fun logException(message: String, exception: Throwable) {
        log(message, LogLevel.ERROR)
        log(exception.stackTraceToString(), LogLevel.ERROR)
    }
}

private class GradleLoggerWrapper(private val logger: GradleLogger) : LoggerImpl {
    override fun log(message: String, level: LogLevel) {
        when (level) {
            LogLevel.TRACE -> logger.trace(message)
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.INFO -> logger.lifecycle(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
        }
    }
}

private class PrintlnLogger : LoggerImpl {
    override fun log(message: String, level: LogLevel) {
        val output = if (level == LogLevel.ERROR) System.err else System.out
        output.println(Logger.format(message, level))
    }
}

private class EchoLogger(private val context: CliktCommand) : LoggerImpl {
    override fun log(message: String, level: LogLevel) {
        context.echo(Logger.format(message, level), err = level == LogLevel.ERROR)
    }
}

/**
 * Abstraction layer for multiple logging implementations.
 */
class Logger private constructor(private val logger: LoggerImpl, private val level: LogLevel) {

    fun log(message: String, level: LogLevel) {
        if (!shouldLog(level)) return
        logger.log(message, level)
    }
    private fun shouldLog(level: LogLevel): Boolean = level >= this.level

    fun info(message: String) = log(message, LogLevel.INFO)
    fun warn(message: String) = log(message, LogLevel.WARN)
    fun error(message: String) = log(message, LogLevel.ERROR)
    fun debug(message: String) = log(message, LogLevel.DEBUG)
    fun trace(message: String) = log(message, LogLevel.TRACE)

    fun error(message: String, exception: Throwable) {
        if (shouldLog(LogLevel.ERROR)) logger.logException(message, exception)
    }


    companion object {
        /**
         * Simple formatter used by most loggers and for pre-logger-initialization logging.
         */
        fun format(message: String, level: LogLevel) = "[${level.name}] $message"

        fun gradle(logger: GradleLogger, level: LogLevel = LogLevel.INFO): Logger = Logger(GradleLoggerWrapper(logger), level)
        fun echo(context: CliktCommand, level: LogLevel = LogLevel.INFO): Logger = Logger(EchoLogger(context), level)
        fun println(level: LogLevel = LogLevel.INFO): Logger = Logger(PrintlnLogger(), level)
    }
}