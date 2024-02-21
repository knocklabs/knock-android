package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonProperty

enum class FeedItemScope {
    @JsonProperty("all") ALL,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("read") READ,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("seen") SEEN,
}