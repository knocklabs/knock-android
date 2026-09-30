package app.knock.client

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
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

class Knock private constructor(internal val context: Context) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val KNOCK_COROUTINE_CONTEXT by lazy { SupervisorJob() }
        internal val coroutineScope = CoroutineScope(KNOCK_COROUTINE_CONTEXT)

        internal val KNOCK_PENDING_NOTIFICATION_KEY = "knock_pending_notification_key"
        internal val KNOCK_MESSAGE_ID_KEY = "knock_message_id"
        internal val KNOCK_PUSH_TAP_HANDLED_KEY = "knock_push_tap_handled"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sharedInstance: Knock? = null
        val shared: Knock
            get() {
                sharedInstance?.let { return it }
                throw KnockException.KnockSetupError
            }

        /** The shared instance, or null when [setup] hasn't been called (e.g. in a service started before the app). */
        internal val sharedOrNull: Knock?
            get() = sharedInstance

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

            if (sharedInstance == null) {
                sharedInstance = Knock(context)
            }

            (context.applicationContext as? Application)?.let { application ->
                application.unregisterActivityLifecycleCallbacks(shared)
                application.registerActivityLifecycleCallbacks(shared)
            }


            shared.logger.loggingDebugOptions = options?.loggingOptions ?: KnockLoggingOptions.ERRORS_ONLY
            shared.environment.setSharedPreferences(context)
            shared.environment.setPublishableKey(key = publishableKey)
            shared.environment.setBaseUrl(baseUrl = options?.hostname)
            shared.environment.setPushChannelId(pushChannelId)
        }
    }

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

    @Volatile
    var feedManager: FeedManager? = null

    // Activity callbacks arrive on the main thread. Counting started activities keeps the feed connected while
    // navigating between activities (B.onStart runs before A.onStop) and disconnects only when the app is backgrounded.
    private var startedActivityCount = 0

    override fun onActivityStarted(activity: Activity) {
        startedActivityCount++
        if (startedActivityCount == 1) {
            feedManager?.resumeConnection()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = maxOf(0, startedActivityCount - 1)
        if (startedActivityCount == 0) {
            feedManager?.disconnectFromFeed()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

data class KnockStartupOptions(
    val hostname: String? = null,
    val loggingOptions: KnockLoggingOptions = KnockLoggingOptions.ERRORS_ONLY
)


