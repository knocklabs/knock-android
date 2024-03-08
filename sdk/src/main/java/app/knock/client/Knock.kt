package app.knock.client

import android.content.Context
import app.knock.client.models.KnockException
import app.knock.client.modules.AuthenticationModule
import app.knock.client.modules.ChannelModule
import app.knock.client.modules.FeedManager
import app.knock.client.modules.MessageModule
import app.knock.client.modules.PreferenceModule
import app.knock.client.modules.UserModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

object Knock {

    private val KNOCK_COROUTINE_CONTEXT by lazy { SupervisorJob() }
    internal val coroutineScope = CoroutineScope(KNOCK_COROUTINE_CONTEXT)

    internal val KNOCK_PENDING_NOTIFICATION_KEY = "knock_pending_notification_key"
    internal val KNOCK_MESSAGE_ID_KEY = "knock_message_id"

    internal val httpClient by lazy {
        OkHttpClient.Builder().build()
    }

    internal var environment = KnockEnvironment()
    internal val logger = KnockLogger()

    internal val authenticationModule by lazy { AuthenticationModule() }
    internal val userModule by lazy { UserModule() }
    internal val preferenceModule by lazy { PreferenceModule() }
    internal val messageModule by lazy { MessageModule() }
    internal val channelModule by lazy { ChannelModule() }

    var feedManager: FeedManager? = null

    /**
     * Sets all the needed information for your global Knock instance.
     *
     * @param publishableKey Your public API key.
     * @param pushChannelId Optional push channel ID.
     * @param options Optional options for customizing the Knock instance.
     */
    @Throws(Exception::class)
    fun setup(context: Context, publishableKey: String, pushChannelId: String?, options: KnockStartupOptions? = null) {
        if (publishableKey.startsWith("sk_")) {
            throw KnockException.WrongKeyError
        }

        logger.loggingDebugOptions = options?.loggingOptions ?: KnockLoggingOptions.ERRORS_ONLY
        environment.setSharedPreferences(context)
        environment.setPublishableKey(key = publishableKey)
        environment.setBaseUrl(baseUrl = options?.hostname)
        environment.setPushChannelId(pushChannelId)
    }

    /**
     * Resets the current global Knock instance entirely.
     * After calling this, you will need to setup and sign in again.
     */
    fun resetInstance() {
        environment = KnockEnvironment()
    }
}

data class KnockStartupOptions(
    val hostname: String? = null,
    val loggingOptions: KnockLoggingOptions = KnockLoggingOptions.ERRORS_ONLY
)


