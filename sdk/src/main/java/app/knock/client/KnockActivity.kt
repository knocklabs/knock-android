package app.knock.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.RemoteMessage
open class KnockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // See if there is a pending click event
        checkForPushNotificationClick(intent)

        // Handle delivered messages on the main thread
//        Courier.shared.getLastDeliveredMessage { message ->
//            onPushNotificationDelivered(message)
//        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForPushNotificationClick(intent)
    }

    private fun checkForPushNotificationClick(intent: Intent?) {
        intent?.getPushNotificationFromTap { message ->
            onPushNotificationClicked(message)
        }
    }

    open fun onPushNotificationClicked(message: RemoteMessage) {}
}

open class KnockComponentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // See if there is a pending click event
        checkForPushNotificationClick(intent)

        // Handle delivered messages on the main thread
//        Courier.shared.getLastDeliveredMessage { message ->
//            onPushNotificationDelivered(message)
//        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForPushNotificationClick(intent)
    }

    private fun checkForPushNotificationClick(intent: Intent?) {
        intent?.getPushNotificationFromTap { message ->
            onPushNotificationClicked(message)
        }
    }

    open fun onPushNotificationClicked(message: RemoteMessage) {}
}