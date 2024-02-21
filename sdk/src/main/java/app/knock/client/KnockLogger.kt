package app.knock.client

import android.util.Log

class KnockLogger {
    private companion object val loggingSubsystem = "knock-swift"

    var loggingDebugOptions: KnockLoggingOptions = KnockLoggingOptions.ERRORS_ONLY

    fun log(
        type: LogType = LogType.DEBUG,
        category: LogCategory,
        message: String,
        description: String? = null,
        status: LogStatus? = null,
        errorMessage: String? = null,
        additionalInfo: Map<String, String>? = null
    ) {
        when (loggingDebugOptions) {
            KnockLoggingOptions.ERRORS_ONLY -> if (type != LogType.ERROR) return
            KnockLoggingOptions.ERRORS_AND_WARNINGS_ONLY -> if (type != LogType.ERROR && type != LogType.WARNING) return
            KnockLoggingOptions.VERBOSE -> Unit // Do nothing, log everything
            KnockLoggingOptions.NONE -> return
        }

        var composedMessage = "[Knock] $message"
        description?.let { composedMessage += " | Description: $it" }
        status?.let { composedMessage += " | Status: ${it.name}" }
        errorMessage?.let { composedMessage += " | Error: $it" }
        additionalInfo?.forEach { (key, value) ->
            composedMessage += " | $key: $value"
        }

        // Using Android Log API for logging
        val tag = "${loggingSubsystem.capitalize()}:${category.name.capitalize()}"
        when (type) {
            LogType.DEBUG -> Log.d(tag, composedMessage)
            LogType.INFO -> Log.i(tag, composedMessage)
            LogType.ERROR -> Log.e(tag, composedMessage)
            LogType.WARNING -> Log.w(tag, composedMessage)
            LogType.LOG -> Log.v(tag, composedMessage) // There's no direct equivalent to 'log', using verbose
        }
    }

    enum class LogStatus {
        SUCCESS, FAIL
    }

    enum class LogType {
        DEBUG, INFO, ERROR, WARNING, LOG
    }

    enum class LogCategory {
        USER, FEED, CHANNEL, PREFERENCES, NETWORKING, PUSH_NOTIFICATION, MESSAGE, GENERAL, APP_DELEGATE
    }
}

// Extension function for easy logging within the Knock object
fun Knock.log(
    type: KnockLogger.LogType = KnockLogger.LogType.DEBUG,
    category: KnockLogger.LogCategory,
    message: String,
    description: String? = null,
    status: KnockLogger.LogStatus? = null,
    errorMessage: String? = null,
    additionalInfo: Map<String, String>? = null
) {
    logger.log(type, category, message, description, status, errorMessage, additionalInfo)
}
