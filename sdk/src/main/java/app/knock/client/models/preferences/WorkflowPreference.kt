package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowPreference(
    var channelTypes: ChannelTypePreferences = ChannelTypePreferences(),
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var conditions: List<Condition> = listOf(),
)

object BooleanOrWorkflowPreferenceDeserializer : JsonDeserializer<Either<Boolean, WorkflowPreference>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Either<Boolean, WorkflowPreference> {
        return try {
            Either.Left(p.readValueAs(Boolean::class.java))
        } catch (e: Exception) {
            Either.Right(p.readValueAs(WorkflowPreference::class.java))
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