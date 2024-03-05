package app.knock.client

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize


//enum class ChannelTypeKey {
//    @JsonProperty("email") EMAIL,
//    @JsonProperty("in_app_feed") IN_APP_FEED,
//    @JsonProperty("sms") SMS,
//    @JsonProperty("push") PUSH,
//    @JsonProperty("chat") CHAT,
//}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Condition(
    val variable: String,
    var operator: String,
    var argument: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionsArray(
    var conditions: List<Condition>,
)

object BooleanOrConditionsArrayDeserializer : JsonDeserializer<Either<Boolean, ConditionsArray>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Either<Boolean, ConditionsArray> {
        try {
            return Either.Left(p.readValueAs(Boolean::class.java))
        }
        catch (e: Exception) {
            return Either.Right(p.readValueAs(ConditionsArray::class.java))
        }
    }
}

object BooleanOrConditionsArraySerializer : JsonSerializer<Either<Boolean, ConditionsArray>>() {
    override fun serialize(value: Either<Boolean, ConditionsArray>, gen: JsonGenerator, serializers: SerializerProvider) {
        with(gen) {
            when (value) {
                is Either.Left -> writeBoolean(value.value)
                is Either.Right -> writeObject(value.value)
            }
        }
    }
}

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowPreference(
    @JsonSetter(nulls = Nulls.SKIP)
    var channelTypes: ChannelTypePreferences = ChannelTypePreferences(),

    @JsonSetter(nulls = Nulls.SKIP)
    var conditions: List<Condition> = listOf(),
)

object BooleanOrWorkflowPreferenceDeserializer : JsonDeserializer<Either<Boolean, WorkflowPreference>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Either<Boolean, WorkflowPreference> {
        try {
            return Either.Left(p.readValueAs(Boolean::class.java))
        }
        catch (e: Exception) {
            return Either.Right(p.readValueAs(WorkflowPreference::class.java))
        }
    }
}

object BooleanOrWorkflowPreferenceSerializer : JsonSerializer<Either<Boolean, WorkflowPreference>>() {
    override fun serialize(value: Either<Boolean, WorkflowPreference>, gen: JsonGenerator, serializers: SerializerProvider) {
        with(gen) {
            when (value) {
                is Either.Left -> writeBoolean(value.value)
                is Either.Right -> writeObject(value.value)
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceSet (
    var id: String? = "default", // default or tenant.id; TODO: check this, because the API allows any value to be used here, not only default and an existing tenant.id

    @JsonSetter(nulls = Nulls.SKIP)
    var channelTypes: ChannelTypePreferences = ChannelTypePreferences(),

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var workflows: Map<String, Either<Boolean, WorkflowPreference>> = mapOf(),

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var categories: Map<String, Either<Boolean, WorkflowPreference>> = mapOf(),
)

fun Knock.getAllUserPreferences(completionHandler: (Result<List<PreferenceSet>>) -> Unit) {
    api.decodeFromGet("/users/$userId/preferences", null, completionHandler)
}

fun Knock.getUserPreferences(preferenceId: String, completionHandler: (Result<PreferenceSet>) -> Unit) {
    api.decodeFromGet("/users/$userId/preferences/$preferenceId", null, completionHandler)
}

fun Knock.setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet, completionHandler: (Result<PreferenceSet>) -> Unit) {
    api.decodeFromPut("/users/$userId/preferences/$preferenceId", preferenceSet, completionHandler)
}