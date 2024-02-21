package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Feed(
    var entries: List<FeedItem> = listOf(),
    var meta: FeedMetadata = FeedMetadata(),
    var pageInfo: PageInfo = PageInfo(),
)
