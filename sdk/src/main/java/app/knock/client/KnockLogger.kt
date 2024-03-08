package app.knock.client

import android.util.Log
import okhttp3.Request
import okhttp3.Response

class KnockLogger {
    private companion object val loggingSubsystem = "knock-android"

    var loggingDebugOptions: KnockLoggingOptions = KnockLoggingOptions.ERRORS_ONLY

    fun log(
        type: LogType = LogType.DEBUG,
        category: KnockLogCategory,
        message: String,
        description: String? = null,
        exception: Exception? = null,
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
        exception?.let { composedMessage += " | Error: ${it.localizedMessage}" }
        additionalInfo?.forEach { (key, value) ->
            composedMessage += " | $key: $value"
        }

        // Using Android Log API for logging
        val tag = "${loggingSubsystem}:${category.name}"
        when (type) {
            LogType.DEBUG -> Log.d(tag, composedMessage)
            LogType.INFO -> Log.i(tag, composedMessage)
            LogType.ERROR -> Log.e(tag, composedMessage)
            LogType.WARNING -> Log.w(tag, composedMessage)
            LogType.LOG -> Log.v(tag, composedMessage) // There's no direct equivalent to 'log', using verbose
        }
    }

    enum class LogType {
        DEBUG, INFO, ERROR, WARNING, LOG
    }
}

// Extension function for easy logging within the Knock object
fun Knock.logDebug(
    category: KnockLogCategory,
    message: String,
    description: String? = null,
    exception: Exception? = null,
    additionalInfo: Map<String, String>? = null
) {
    logger.log(KnockLogger.LogType.DEBUG, category, message, description, exception, additionalInfo)
}

fun Knock.logError(
    category: KnockLogCategory,
    message: String,
    description: String? = null,
    exception: Exception? = null,
    additionalInfo: Map<String, String>? = null
) {
    logger.log(KnockLogger.LogType.ERROR, category, message, description, exception, additionalInfo)
}

fun Knock.logWarning(
    category: KnockLogCategory,
    message: String,
    description: String? = null,
    exception: Exception? = null,
    additionalInfo: Map<String, String>? = null
) {
    logger.log(KnockLogger.LogType.WARNING, category, message, description, exception, additionalInfo)
}

fun Knock.logNetworking(
    message: String,
    description: String? = null,
    exception: Exception? = null,
    request: Request? = null,
    response: Response? = null
) {
    val data: MutableMap<String, String> = mutableMapOf()

    request?.let {
        data["URL"] = it.url.toString()
        data["METHOD"] = it.method
        it.body?.let { body ->
            data["BODY"] = body.toString()
        }
    }

    response?.let {
        data["STATUS"] = if (it.isSuccessful) "SUCCESS" else "FAIL"
        data["CODE"] = "${it.code} : ${it.message}"
        it.body?.let { body ->
            data["RESPONSE_BODY"] = body.toString()
        }
    }
    logger.log(KnockLogger.LogType.DEBUG, KnockLogCategory.NETWORKING, message, description, exception, data)
}

enum class KnockLogCategory {
    USER, FEED, CHANNEL, PREFERENCES, NETWORKING, PUSH_NOTIFICATION, MESSAGE, GENERAL, APP_DELEGATE
}

enum class KnockLoggingOptions {
    ERRORS_ONLY,
    ERRORS_AND_WARNINGS_ONLY,
    VERBOSE,
    NONE
}
