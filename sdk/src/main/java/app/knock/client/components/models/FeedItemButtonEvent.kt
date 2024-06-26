package app.knock.client.components.models

import app.knock.client.models.feed.BlockActionButton
import app.knock.client.models.feed.FeedItem

data class FeedItemButtonEvent(val feedItem: FeedItem, val actionButton: BlockActionButton)
