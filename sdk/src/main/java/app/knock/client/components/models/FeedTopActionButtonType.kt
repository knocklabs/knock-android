package app.knock.client.components.models

sealed class FeedTopActionButtonType(val title: String) {
    class MarkAllAsRead(title: String = "Mark all as read") : FeedTopActionButtonType(title)
    class ArchiveRead(title: String = "Archive read") : FeedTopActionButtonType(title)
    class ArchiveAll(title: String = "Archive all") : FeedTopActionButtonType(title)
}
