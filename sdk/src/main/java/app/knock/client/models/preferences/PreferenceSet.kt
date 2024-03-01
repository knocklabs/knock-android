package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceSet (
    var id: String = "default",
    var channelTypes: ChannelTypePreferences = ChannelTypePreferences(),

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var workflows: Map<String, Either<Boolean, WorkflowPreference>>?,

    @JsonDeserialize(contentUsing = BooleanOrWorkflowPreferenceDeserializer::class)
    @JsonSerialize(contentUsing = BooleanOrWorkflowPreferenceSerializer::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    var categories: Map<String, Either<Boolean, WorkflowPreference>>?,
)
