package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonProperty

enum class KnockMessageStatus {
    @JsonProperty("queued") QUEUED,
    @JsonProperty("sent") SENT,
    @JsonProperty("delivered") DELIVERED,
    @JsonProperty("delivery_attempted") DELIVERY_ATTEMPTED,
    @JsonProperty("undelivered") UNDELIVERED,
    @JsonProperty("seen") SEEN,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("unarchived") UNARCHIVED,
}

enum class KnockMessageStatusUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("unarchived") UNARCHIVED,
}