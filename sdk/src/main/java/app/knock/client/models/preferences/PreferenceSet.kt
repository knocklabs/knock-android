package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceSet (
    var id: String = "default",

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var channelTypes: ChannelTypePreferences = ChannelTypePreferences(),

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var workflows: Map<String, Either<Boolean, WorkflowPreference>> = mapOf(),

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var categories: Map<String, Either<Boolean, WorkflowPreference>>? = mapOf(),

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(contentUsing = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrConditionsArraySerializer::class)
    var channels: Map<String, Either<Boolean, ConditionsArray>>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = BooleanOrConditionsArrayDeserializer::class)
    @JsonSerialize(using = BooleanOrConditionsArraySerializer::class)
    var commercialSubscribed: Either<Boolean, ConditionsArray>? = null,
)
