package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonProperty

enum class FeedItemArchivedScope {
    @JsonProperty("include") INCLUDE,
    @JsonProperty("exclude") EXCLUDE,
    @JsonProperty("only") ONLY,
}