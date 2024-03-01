package app.knock.client.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

// Channels

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelData(
    var channelId: String,

    @JsonAnySetter
    @get:JsonAnyGetter
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: MutableMap<String, Any> = hashMapOf(),
)