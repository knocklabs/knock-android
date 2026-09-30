package app.knock.client

import android.annotation.SuppressLint
import android.util.Log
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.registerTokenForFCM
import app.knock.client.modules.updateMessageStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
open class KnockMessagingService: FirebaseMessagingService() {

    /*
    This will be called if user receives a push notification with the app in the foreground only, unless it is a "silent notification".
    A silent notification removes the `notification` object from the payload, which will not trigger the Android OS to handle the notification for you.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Knock.sharedOrNull?.let { knock ->
            knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "received message: $message")
            knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "From: ${message.from}")

            message.data.isNotEmpty().let {
                knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message data payload: " + message.data)
            }

            message.notification?.let {
                knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message Notification Body: ${it.body}")
            }
        } ?: logSetupMissing("onMessageReceived")

        fcmRemoteMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val knock = Knock.sharedOrNull ?: return logSetupMissing("onNewToken")
        knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", token)

        try {
            val channelId = knock.environment.getSafePushChannelId()
            knock.registerTokenForFCM(channelId = channelId, token = token) { result ->
                result.onSuccess {
                    knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Success")
                }.onFailure {
                    knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure")
                }
            }
        } catch (e: Exception) {
            knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure. Need to first set PushChannelId with Knock.setup().")
        }
    }

    open fun fcmRemoteMessageReceived(message: RemoteMessage) {
        val knock = Knock.sharedOrNull ?: return
        message.data[Knock.KNOCK_MESSAGE_ID_KEY]?.let {
            knock.updateMessageStatus(it, KnockMessageStatusUpdateType.READ) {}
        }
    }

    // FCM can start this service in a new process before any Activity; Knock.setup() belongs in Application.onCreate().
    private fun logSetupMissing(event: String) {
        Log.w("knock-android", "[Knock] $event: Knock.setup() has not been called in this process. Call it from Application.onCreate().")
    }
}
