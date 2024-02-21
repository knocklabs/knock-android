package app.knock.client

import android.content.Context
import android.content.SharedPreferences
import app.knock.client.modules.AuthenticationModule
import app.knock.client.modules.ChannelModule
import app.knock.client.modules.FeedManager
import app.knock.client.modules.MessageModule
import app.knock.client.modules.PreferenceModule
import app.knock.client.modules.UserModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Returns a new instance of the Knock Client
 *
 * @param publishableKey your public API key
 * @param userId the user-id that will be used in the subsequent method calls
 * @param userToken (optional) user token. Used in production when enhanced security is enabled
 * @param hostname (optional) custom hostname of the API, including schema (https://)
 */
//class Knock(
//    var publishableKey: String,
//    var userId: String,
//    var userToken: String? = null,
//    hostname: String? = null
//) {
//    var api: KnockAPI
//
//    init {
//        if (publishableKey.startsWith("sk_")) {
//            throw Exception("[Knock] You are using your secret API key on the client. Please use the public key.")
//        }
//        this.api = KnockAPI(publishableKey, userToken, hostname)
//    }
//}


object Knock {
    const val clientVersion = "1.0.0"

    fun initialize(context: Context) {
        if (!initialized) {
            val appContext = context.applicationContext
            sharedPreferences = appContext.getSharedPreferences("knock-android", Context.MODE_PRIVATE)
            initialized = true
        }
    }
    private var initialized = false
    private lateinit var sharedPreferences: SharedPreferences
    private val KNOCK_COROUTINE_CONTEXT by lazy { SupervisorJob() }
    internal val coroutineScope = CoroutineScope(KNOCK_COROUTINE_CONTEXT)

    var feedManager: FeedManager? = null
    internal val environment = KnockEnvironment()
    internal val authenticationModule by lazy { AuthenticationModule() }
    internal val userModule by lazy { UserModule() }
    internal val preferenceModule by lazy { PreferenceModule() }
    internal val messageModule by lazy { MessageModule() }
    internal val channelModule by lazy { ChannelModule() }
    internal val logger = KnockLogger()

    /**
     * Sets up a new instance of the Knock Client.
     *
     * @param publishableKey Your public API key.
     * @param pushChannelId Optional push channel ID.
     * @param options Optional options for customizing the Knock instance.
     */
    suspend fun setup(publishableKey: String, pushChannelId: String?, options: KnockStartupOptions? = null) {
        logger.loggingDebugOptions = options?.loggingOptions ?: KnockLoggingOptions.ERRORS_ONLY
        environment.setPublishableKey(key = publishableKey)
        environment.setBaseUrl(baseUrl = options?.hostname)
        environment.setPushChannelId(pushChannelId)
    }


    /**
     * Resets the current Knock instance entirely.
     * After calling this, you will need to setup and sign in again.
     */
    fun resetInstanceCompletely() {
        // TODO
        // Since Knock is an object (singleton), it cannot be reassigned like in Swift.
        // You'll need to reset its properties manually if needed.
    }

    fun getSharedPreferences(): SharedPreferences {
        if (!initialized) {
            throw IllegalStateException("SDKInitializer is not initialized. Call SDKInitializer.initialize(Context) first.")
        }
        return sharedPreferences
    }
}

data class KnockStartupOptions(
    val hostname: String? = null,
    val loggingOptions: KnockLoggingOptions = KnockLoggingOptions.ERRORS_ONLY
)

enum class KnockLoggingOptions {
    ERRORS_ONLY,
    ERRORS_AND_WARNINGS_ONLY,
    VERBOSE,
    NONE
}
