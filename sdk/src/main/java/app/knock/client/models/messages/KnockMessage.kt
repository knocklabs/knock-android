package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class KnockMessage(
    val id: String,
    val channelId: String,
    val recipient: Any,
    var workflow: String,
    var tenant: String?,
    var status: KnockMessageStatus,
    var engagementStatuses: List<KnockMessageEngagementStatus>,
    var seenAt: ZonedDateTime?,
    var readAt: ZonedDateTime?,
    var interactedAt: ZonedDateTime?,
    var linkClickedAt: ZonedDateTime?,
    var archivedAt: ZonedDateTime?,
    var source: WorkflowSource,

    @JsonAnySetter
    @get:JsonAnyGetter
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf()
)
