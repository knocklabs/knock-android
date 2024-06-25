package app.knock.client.components.models

import android.content.Context
import app.knock.client.R
import app.knock.client.components.themes.EmptyFeedViewTheme
import app.knock.client.models.feed.FeedItemScope

data class InAppFeedFilter(
    val context: Context,
    val scope: FeedItemScope,
    val title: String = defaultTitle(scope),
    val emptyViewTheme: EmptyFeedViewTheme = defaultEmptyViewConfig(context, scope)
) {
    companion object {
        fun defaultEmptyViewConfig(context: Context, scope: FeedItemScope): EmptyFeedViewTheme {
            return when (scope) {
                FeedItemScope.ARCHIVED -> EmptyFeedViewTheme(
                    context,
                    title = "No archived messages",
                    subtitle = "Any notifications you archive will show up here",
                    iconResId = R.drawable.tray // Assuming you have a drawable resource `ic_tray`
                )
                FeedItemScope.UNREAD -> EmptyFeedViewTheme(
                    context,
                    title = "No unread messages",
                    subtitle = "Any notifications you haven't read show up here",
                    iconResId = R.drawable.tray
                )
                FeedItemScope.UNSEEN -> EmptyFeedViewTheme(
                    context,
                    title = "No unseen messages",
                    subtitle = "Any notifications you haven't seen will show up here",
                    iconResId = R.drawable.tray
                )
                else ->
                    EmptyFeedViewTheme(
                        context,
                        title = "All caught up",
                        subtitle = "You’ll see previously read and new notifications here",
                        iconResId = R.drawable.tray
                    )
            }
        }

        fun defaultTitle(scope: FeedItemScope): String {
            return when (scope) {
                FeedItemScope.ARCHIVED -> "Archived"
                FeedItemScope.ALL -> "All"
                FeedItemScope.UNREAD -> "Unread"
                FeedItemScope.UNSEEN -> "Unseen"
                FeedItemScope.READ -> "Read"
                FeedItemScope.SEEN -> "Seen"
                FeedItemScope.INTERACTED -> "Interacted"
            }
        }
    }
}
