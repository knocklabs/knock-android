package app.knock.client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import app.knock.client.models.KnockException
import com.google.firebase.messaging.RemoteMessage

// This is just an example of how you could present a notification with the app in the foreground.
// You should customize this to fit your own app's needs.
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

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannel(channelId, settingsTitle, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val uuid = System.currentTimeMillis().toInt()
        notificationManager.notify(uuid, notificationBuilder.build())

    } catch (e: Exception) {
        Knock.logError(
            KnockLogCategory.PUSH_NOTIFICATION,
            "RemoteMessage.presentNotification",
            exception = e
        )
    }
}