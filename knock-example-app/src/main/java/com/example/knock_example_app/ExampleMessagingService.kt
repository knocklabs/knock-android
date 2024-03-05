package com.example.knock_example_app

import android.annotation.SuppressLint
import app.knock.client.KnockMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ExampleMessagingService: KnockMessagingService() {
    override fun showNotification(message: RemoteMessage) {
//        message.presentNotification(
//            context = this,
//            handlingClass = MainActivity::class.java,
//            icon = android.R.drawable.ic_dialog_info
//        )
    }
}