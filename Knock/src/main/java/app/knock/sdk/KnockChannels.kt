package app.knock.sdk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// Channels

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelData(
    var channelId: String,
    var data: MutableMap<String, Any> = hashMapOf(),
)