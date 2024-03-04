package com.example.knock_example_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.knock.client.Knock
import app.knock.client.modules.registerTokenForFCM
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await

class ExampleMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("onMessageReceived", "recieved message: $message")

        Log.d("onMessageReceived", "From: ${message.from}")

        // Check if message contains a data payload.
        message.data.isNotEmpty().let {
            Log.d("onMessageReceived", "Message data payload: " + message.data)
        }

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d("onMessageReceived", "Message Notification Body: ${it.body}")
            // You can also implement notification building logic here
        }

        message.presentNotification(
            context = this,
            handlingClass = MainActivity::class.java,
            icon = android.R.drawable.ic_dialog_info
        )
        // Try and show the notification
//        showNotification(message)

    }
//    dozXo0neTkuAcWXPEwuERf:APA91bGR7L9fM7Abk_OG59_lzy4nNgaHSabHlFbiCZMzP8L4LxF15qnYWYbnoWkJTDpBduHV_XZJkrf1DnKUXHq-7aNmJ8LBuD15seifmv6XujZkW_M6z4VHyTxyTm0I_Qgh-BttLTun
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("onNewToken", token)
        Knock.registerTokenForFCM(channelId = Utils.pushChannelId, token = token) { result ->
            result.onSuccess {
                Log.d("onNewToken", "registerTokenForFCM: Success")
            }.onFailure {
                Log.d("onNewToken", "registerTokenForFCM: Failure")
            }
        }
    }
    open fun showNotification(message: RemoteMessage) {
        // Empty
    }
}

fun RemoteMessage.presentNotification(context: Context, handlingClass: Class<*>?, icon: Int, settingsTitle: String = "Notification settings") {

    try {

        val channelId = "default"
        val pendingIntent = CourierIntent(context, handlingClass, this).pendingIntent
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = data["title"] ?: notification?.title ?: "Empty Title"
        val body = data["body"] ?: notification?.body ?: "Empty Body"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, settingsTitle, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val uuid = System.currentTimeMillis().toInt()
        notificationManager.notify(uuid, notificationBuilder.build())

    } catch (e: Exception) {

        print(e)

    }

}

internal class KnockIntent(private val context: Context, cls: Class<*>?, message: RemoteMessage) : Intent(context, cls) {
    init {
        putExtra("courier_pending_notification_key", message)
        addCategory(CATEGORY_LAUNCHER)
        addFlags(FLAG_ACTIVITY_SINGLE_TOP)
        action = ACTION_MAIN
    }

    internal val pendingIntent get() = PendingIntent.getActivity(
        context,
        0,
        this,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}