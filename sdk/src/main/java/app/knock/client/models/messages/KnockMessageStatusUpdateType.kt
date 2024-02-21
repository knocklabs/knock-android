package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonProperty

enum class KnockMessageStatusUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
}
