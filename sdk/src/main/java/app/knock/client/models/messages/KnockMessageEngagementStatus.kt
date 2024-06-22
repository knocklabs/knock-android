package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("unused")
enum class KnockMessageEngagementStatus {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("link_clicked") LINK_CLICKED,
    @JsonProperty("archived") ARCHIVED,
}