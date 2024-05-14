package app.knock.client.components.models

import app.knock.client.R

enum class FeedNotificationRowSwipeAction(val title: String, val imageResId: Int, val colorResId: Int) {
    Archive("Archive", R.drawable.archive, R.color.green9),
    MarkAsRead("Read", R.drawable.mail_open, R.color.blue9),
    MarkAsUnread("Unread", R.drawable.mail_closed, R.color.blue9);

    // Assuming SwipeButtonConfig is a data class you've defined in Kotlin
    val defaultConfig: SwipeButtonConfig
        get() = SwipeButtonConfig(this, title, imageResId, colorResId)
}

// Assuming you have the following classes in Kotlin
data class SwipeButtonConfig(
    val action: FeedNotificationRowSwipeAction,
    val title: String,
    val imageResId: Int,
    val colorResId: Int
)
