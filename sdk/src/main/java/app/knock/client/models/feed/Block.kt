package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Block(
    var content: String,
    var name: String,
    var rendered: String,
)
