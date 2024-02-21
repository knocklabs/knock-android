package app.knock.client.models.preferences

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceSet (
    var id: String? = "default", // default or tenant.id; TODO: check this, because the API allows any value to be used here, not only default and an existing tenant.id
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
