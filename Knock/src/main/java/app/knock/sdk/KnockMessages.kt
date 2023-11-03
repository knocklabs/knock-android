package app.knock.sdk

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

// Messages

enum class KnockMessageStatus {
    @JsonProperty("queued") QUEUED,
    @JsonProperty("sent") SENT,
    @JsonProperty("delivered") DELIVERED,
    @JsonProperty("delivery_attempted") DELIVERY_ATTEMPTED,
    @JsonProperty("undelivered") UNDELIVERED,
    @JsonProperty("seen") SEEN,
    @JsonProperty("unseen") UNSEEN,
}

enum class KnockMessageEngagementStatus {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("link_clicked") LINK_CLICKED,
    @JsonProperty("archived") ARCHIVED,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowSource(
    var key: String,
    @JsonProperty("version_id") var versionId: String,
)

//sealed class Either<out L, out R>
//data class Left<out L>(val l: L) : Either<L, Nothing>()
//data class Right<out R>(val r: R) : Either<Nothing, R>()
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//data class RecipientIdentifier(
//    var id: String,
//    var collection: String,
//)

@JsonIgnoreProperties("type")
data class KnockMessage(
    val id: String,
    val channelId: String,
    val recipient: Any,
    var workflow: String,
    var tenant: String?, // the documentation (https://docs.knock.app/reference#messages) says that it's not optional but it can be, so it's declared optional here. CHECK THIS on the docs
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

enum class KnockMessageStatusUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
}

enum class KnockMessageStatusBatchUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("interacted") INTERACTED,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("unarchived") UNARCHIVED,
}