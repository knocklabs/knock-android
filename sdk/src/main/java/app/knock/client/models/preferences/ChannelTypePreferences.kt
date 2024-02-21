package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
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
)


//enum class ChannelTypeKey {
//    @JsonProperty("email") EMAIL,
//    @JsonProperty("in_app_feed") IN_APP_FEED,
//    @JsonProperty("sms") SMS,
//    @JsonProperty("push") PUSH,
//    @JsonProperty("chat") CHAT,
//}
