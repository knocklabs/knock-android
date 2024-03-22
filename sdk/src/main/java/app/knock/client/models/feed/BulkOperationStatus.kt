package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonProperty

enum class BulkOperationStatus {
    @JsonProperty("queued") QUEUED,
    @JsonProperty("processing") PROCESSING,
    @JsonProperty("completed") COMPLETED,
    @JsonProperty("failed") FAILED,
}
