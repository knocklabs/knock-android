package app.knock.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.knock.client.Knock
import app.knock.client.KnockComponentActivity
import app.knock.client.modules.requestNotificationPermission
import app.knock.example.theme.KnockAndroidTheme
import app.knock.example.views.StartupView
import com.google.firebase.messaging.RemoteMessage


class MainActivity : KnockComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Knock.shared.requestNotificationPermission(this)

        setContent {
            KnockAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartupView()
                }
            }
        }
    }

    override fun onKnockPushNotificationTappedInBackGround(intent: Intent) {
        super.onKnockPushNotificationTappedInBackGround(intent)
        Log.d(Utils.loggingTag, "tapped in background")
    }

    override fun onKnockPushNotificationTappedInForeground(message: RemoteMessage) {
        super.onKnockPushNotificationTappedInForeground(message)
        Log.d(Utils.loggingTag, "tapped in foreground")
    }
}
