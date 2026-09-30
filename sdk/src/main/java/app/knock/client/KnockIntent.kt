package app.knock.client

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage

internal class KnockIntent(private val context: Context, cls: Class<*>?, message: RemoteMessage) : Intent(context, cls) {
    init {
        putExtra(Knock.KNOCK_PENDING_NOTIFICATION_KEY, message)
        addCategory(CATEGORY_LAUNCHER)
        addFlags(FLAG_ACTIVITY_SINGLE_TOP)
        action = ACTION_MAIN
    }

    // PendingIntents that differ only in extras are considered equal, so each message needs its own request code;
    // with a shared code every new notification replaced (and cancelled) the tap intent of all earlier ones.
    private val requestCode = (message.data[Knock.KNOCK_MESSAGE_ID_KEY] ?: message.messageId ?: "${System.nanoTime()}").hashCode()

    internal val pendingIntent get() = PendingIntent.getActivity(
        context,
        requestCode,
        this,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}