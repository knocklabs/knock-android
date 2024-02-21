package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowSource(
    var key: String,
    @JsonProperty("version_id") var versionId: String,
)
