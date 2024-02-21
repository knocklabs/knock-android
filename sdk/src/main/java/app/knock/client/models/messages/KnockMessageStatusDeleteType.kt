package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonProperty

enum class KnockMessageStatusDeleteType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("archived") ARCHIVED,
}
