package com.example.knock_example_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.knock.client.KnockComponentActivity
import com.example.knock_example_app.ui.theme.KnockandroidTheme
import com.example.knock_example_app.views.StartupView
import com.google.firebase.messaging.RemoteMessage


class MainActivity : KnockComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KnockandroidTheme {
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
