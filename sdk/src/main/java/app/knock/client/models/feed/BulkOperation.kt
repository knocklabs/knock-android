package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class BulkOperation(
    var id: String,
    var name: String,
    var status: BulkOperationStatus,
    var estimatedTotalRows: Int,
    var processedRows: Int,
    var startedAt: ZonedDateTime?,
    var completedAt: ZonedDateTime?,
    var failedAt: ZonedDateTime?,
)
