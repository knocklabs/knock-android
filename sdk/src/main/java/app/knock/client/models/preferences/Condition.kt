package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

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
        return try {
            Either.Left(p.readValueAs(Boolean::class.java))
        } catch (e: Exception) {
            Either.Right(p.readValueAs(ConditionsArray::class.java))
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
