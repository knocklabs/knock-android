package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

@JsonIgnoreProperties(ignoreUnknown = true)
data class Feed(
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var entries: List<FeedItem> = listOf(),
    var meta: FeedMetadata = FeedMetadata(),
    var pageInfo: PageInfo = PageInfo(),
)
