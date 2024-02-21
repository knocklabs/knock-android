package app.knock.client.models.messages

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties("type")
data class KnockMessage(
    val id: String,
    val channelId: String,
    val recipient: Any,
    var workflow: String,
    var tenant: String?, // the documentation (https://docs.knock.app/reference#messages) says that it's not optional but it can be, so it's declared optional here. TODO: check this on the docs
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
    var data: Map<String, Any> = hashMapOf(),
)
