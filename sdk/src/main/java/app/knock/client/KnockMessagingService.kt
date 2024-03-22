package app.knock.client

import android.annotation.SuppressLint
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
        Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "received message: $message")
        Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "From: ${message.from}")

        message.data.isNotEmpty().let {
            Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message data payload: " + message.data)
        }

        message.notification?.let {
            Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message Notification Body: ${it.body}")
        }

        fcmRemoteMessageReceived(message)
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", token)

        try {
            val channelId = Knock.shared.environment.getSafePushChannelId()
            Knock.shared.registerTokenForFCM(channelId = channelId, token = token) { result ->
                result.onSuccess {
                    Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Success")
                }.onFailure {
                    Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure")
                }
            }
        } catch (e: Exception) {
            Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure. Need to first set PushChannelId with Knock.setup().")
        }
    }
    open fun fcmRemoteMessageReceived(message: RemoteMessage) {
        message.data[Knock.KNOCK_MESSAGE_ID_KEY]?.let {
            Knock.shared.updateMessageStatus(it, KnockMessageStatusUpdateType.READ) {}
        }
    }
}