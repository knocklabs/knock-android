package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedSettings(
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val features: FeedFeatures = FeedFeatures(brandingRequired = true)
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedFeatures(
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val brandingRequired: Boolean = true
)