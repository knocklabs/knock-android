package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelTypePreferences(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var email: Either<Boolean, ConditionsArray>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var inAppFeed: Either<Boolean, ConditionsArray>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var sms: Either<Boolean, ConditionsArray>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var push: Either<Boolean, ConditionsArray>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var chat: Either<Boolean, ConditionsArray>? = null,
) {
    fun asArrayOfPreferenceItems(): List<ChannelTypePreferenceItem> {
        return listOfNotNull(
            email?.let { ChannelTypePreferenceItem(ChannelTypeKey.EMAIL, it) },
            inAppFeed?.let { ChannelTypePreferenceItem(ChannelTypeKey.IN_APP_FEED, it) },
            sms?.let { ChannelTypePreferenceItem(ChannelTypeKey.SMS, it) },
            push?.let { ChannelTypePreferenceItem(ChannelTypeKey.PUSH, it) },
            chat?.let { ChannelTypePreferenceItem(ChannelTypeKey.CHAT, it) }
        )
    }
}

data class ChannelTypePreferenceItem(
    val id: ChannelTypeKey,
    var value: Either<Boolean, ConditionsArray>
)
enum class ChannelTypeKey {
    @JsonProperty("email") EMAIL,
    @JsonProperty("in_app_feed") IN_APP_FEED,
    @JsonProperty("sms") SMS,
    @JsonProperty("push") PUSH,
    @JsonProperty("chat") CHAT,
}
