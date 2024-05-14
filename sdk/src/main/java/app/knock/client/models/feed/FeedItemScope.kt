package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonProperty

enum class FeedItemScope {
    @JsonProperty("all") ALL,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("read") READ,
    @JsonProperty("seen") SEEN,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("interacted") INTERACTED
}