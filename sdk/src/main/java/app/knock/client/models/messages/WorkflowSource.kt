package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowSource(
    var key: String,
    var versionId: String,
)
