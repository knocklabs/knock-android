package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonProperty

enum class KnockMessageStatusBatchUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("unarchived") UNARCHIVED,
}