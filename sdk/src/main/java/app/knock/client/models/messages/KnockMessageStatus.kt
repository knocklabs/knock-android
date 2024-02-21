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
}
