package app.knock.client.components.models

import app.knock.client.R
import app.knock.client.models.feed.FeedItemScope

data class InAppFeedFilter(
    val scope: FeedItemScope,
    val title: String,
    val emptyViewConfig: EmptyFeedViewConfig
) {
    companion object {
        fun defaultEmptyViewConfig(scope: FeedItemScope): EmptyFeedViewConfig {
            return when (scope) {
                FeedItemScope.ARCHIVED -> EmptyFeedViewConfig(
                    title = "No archived messages",
                    subtitle = "Any notifications you archive will show up here",
                    icon = R.drawable.tray // Assuming you have a drawable resource `ic_tray`
                )
                FeedItemScope.UNREAD -> EmptyFeedViewConfig(
                    title = "No unread messages",
                    subtitle = "Any notifications you haven't read show up here",
                    icon = R.drawable.tray
                )
                FeedItemScope.UNSEEN -> EmptyFeedViewConfig(
                    title = "No unseen messages",
                    subtitle = "Any notifications you haven't seen will show up here",
                    icon = R.drawable.tray
                )
                else ->
                    EmptyFeedViewConfig(
                        title = "All caught up",
                        subtitle = "You’ll see previously read and new notifications here",
                        icon = R.drawable.tray
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

    constructor(scope: FeedItemScope, title: String? = null, emptyViewConfig: EmptyFeedViewConfig? = null) :
            this(
                scope = scope,
                title = title ?: defaultTitle(scope),
                emptyViewConfig = emptyViewConfig ?: defaultEmptyViewConfig(scope)
            )
}

data class EmptyFeedViewConfig(
    val title: String,
    val subtitle: String,
    val icon: Int // Drawable resource ID
)
