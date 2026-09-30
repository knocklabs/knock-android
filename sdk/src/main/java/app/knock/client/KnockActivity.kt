package app.knock.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.updateMessageStatus
import com.google.firebase.messaging.RemoteMessage

open class KnockActivity : AppCompatActivity(), KnockActivityInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // See if there is a pending tap event from a PushNotification
        checkForPushNotificationTap(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkForPushNotificationTap(intent)
    }
}

open class KnockComponentActivity : ComponentActivity(), KnockActivityInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPushNotificationTap(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkForPushNotificationTap(intent)
    }
}

interface KnockActivityInterface {
    fun onKnockPushNotificationTappedInBackground(intent: Intent) {}
    fun onKnockPushNotificationTappedInForeground(message: RemoteMessage) {}

    fun checkForPushNotificationTap(intent: Intent?) {
        // The same Intent is delivered to onCreate again after a configuration change; handle each tap only once.
        if (intent == null || intent.getBooleanExtra(Knock.KNOCK_PUSH_TAP_HANDLED_KEY, false)) return

        intent.getStringExtra(Knock.KNOCK_MESSAGE_ID_KEY)?.let {
            intent.putExtra(Knock.KNOCK_PUSH_TAP_HANDLED_KEY, true)
            Knock.sharedOrNull?.updateMessageStatus(it, KnockMessageStatusUpdateType.INTERACTED) {}
            onKnockPushNotificationTappedInBackground(intent)
            return
        }

        IntentCompat.getParcelableExtra(intent, Knock.KNOCK_PENDING_NOTIFICATION_KEY, RemoteMessage::class.java)?.let { message ->
            intent.putExtra(Knock.KNOCK_PUSH_TAP_HANDLED_KEY, true)
            message.data[Knock.KNOCK_MESSAGE_ID_KEY]?.let {
                Knock.sharedOrNull?.updateMessageStatus(it, KnockMessageStatusUpdateType.INTERACTED) {}
            }
            onKnockPushNotificationTappedInForeground(message)
        }
    }
}
