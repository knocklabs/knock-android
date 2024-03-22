package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedMetadata(
    var totalCount: Int = 0,
    var unreadCount: Int = 0,
    var unseenCount: Int = 0,
)
