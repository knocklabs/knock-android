package app.knock.client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import app.knock.client.models.KnockException
import com.google.firebase.messaging.RemoteMessage

fun RemoteMessage.presentNotification(context: Context, handlingClass: Class<*>?, icon: Int, settingsTitle: String = "Notification settings") {

    try {
        val channelId = "default"
        val pendingIntent = KnockIntent(context, handlingClass, this).pendingIntent
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
        putExtra(Knock.KNOCK_PENDING_NOTIFICATION_KEY, message)
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

fun Intent.trackPushNotificationClick(onClick: (message: RemoteMessage) -> Unit) {
    try {
        // Check to see if we have an intent to work
        val key = Knock.KNOCK_PENDING_NOTIFICATION_KEY

        (extras?.get(key) as? RemoteMessage)?.let { message ->

            // Clear the intent extra
            extras?.remove(key)

            // Track when the notification was tapped
            Courier.shared.trackNotification(
                message = message,
                event = CourierPushEvent.CLICKED,
                onSuccess = { Courier.log("Event tracked") },
                onFailure = { Courier.error(it.toString()) }
            )

            onClick(message)

        }

    } catch (e: Exception) {
        throw KnockException.RuntimeError(e.toString())
    }

}