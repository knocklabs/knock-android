package com.example.knock_example_app

import android.annotation.SuppressLint
import app.knock.client.KnockMessagingService
import app.knock.client.presentNotification
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ExampleMessagingService: KnockMessagingService() {
    override fun messageReceivedInForeground(message: RemoteMessage) {
        super.messageReceivedInForeground(message)

        // This is just an example of how you could present a notification with the app in the foreground.
        // You should customize this to fit your own app's needs.
        message.presentNotification(
            context = this,
            handlingClass = MainActivity::class.java,
            icon = android.R.drawable.ic_dialog_info
        )
    }
}