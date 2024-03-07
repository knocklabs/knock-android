package app.knock.client

import android.annotation.SuppressLint
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.registerTokenForFCM
import app.knock.client.modules.updateMessageStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
open class KnockMessagingService: FirebaseMessagingService() {

    // This will be called if user receives a push notification with the app in the foreground only. Will not be called if the app is in the background or closed.
    // If you want this method to be called in the background as well, then the message payload can NOT include a "notification" object. Otherwise, its intercepted by the Android OS if in background.
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "received message: $message")
        Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "From: ${message.from}")

        message.data.isNotEmpty().let {
            Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message data payload: " + message.data)
        }

        message.notification?.let {
            Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onMessageReceived", "Message Notification Body: ${it.body}")
        }

        messageReceivedInForeground(message)
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", token)

        try {
            val channelId = Knock.environment.getSafePushChannelId()
            Knock.registerTokenForFCM(channelId = channelId, token = token) { result ->
                result.onSuccess {
                    Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Success")
                }.onFailure {
                    Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure")
                }
            }
        } catch (e: Exception) {
            Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "onNewToken", "registerTokenForFCM: Failure. Need to first set PushChannelId with Knock.setup().")
        }
    }
    open fun messageReceivedInForeground(message: RemoteMessage) {
        message.data[Knock.KNOCK_MESSAGE_ID_KEY]?.let {
            Knock.updateMessageStatus(it, KnockMessageStatusUpdateType.READ) {}
        }
    }
}