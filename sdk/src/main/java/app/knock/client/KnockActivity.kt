package app.knock.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.updateMessageStatus
import com.google.firebase.messaging.RemoteMessage
open class KnockActivity : AppCompatActivity(), KnockActivityInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // See if there is a pending tap event from a PushNotification
        checkForPushNotificationTap(intent)
    }
}

open class KnockComponentActivity : ComponentActivity(), KnockActivityInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPushNotificationTap(intent)
    }
}

interface KnockActivityInterface {
    fun onKnockPushNotificationTappedInBackGround(intent: Intent) {}
    fun onKnockPushNotificationTappedInForeground(message: RemoteMessage) {}

    fun checkForPushNotificationTap(intent: Intent?) {
        intent?.extras?.getString(Knock.KNOCK_MESSAGE_ID_KEY)?.let {
            Knock.shared.updateMessageStatus(it, KnockMessageStatusUpdateType.INTERACTED) {}
            onKnockPushNotificationTappedInBackGround(intent)
        } ?: (intent?.extras?.get(Knock.KNOCK_PENDING_NOTIFICATION_KEY) as? RemoteMessage)?.let { message ->
            // Clear the intent extra
            intent.extras?.remove(Knock.KNOCK_PENDING_NOTIFICATION_KEY)
            message.data[Knock.KNOCK_MESSAGE_ID_KEY]?.let {
                Knock.shared.updateMessageStatus(it, KnockMessageStatusUpdateType.INTERACTED) {}
            }
            onKnockPushNotificationTappedInForeground(message)
        }
    }
}