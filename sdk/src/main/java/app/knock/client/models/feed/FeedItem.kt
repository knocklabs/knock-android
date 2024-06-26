package app.knock.client.models.feed

import app.knock.client.models.KnockUser
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedItem(
    @JsonProperty("__cursor") var feedCursor: String,
    var activities: List<FeedActivity>? = null,
    var actors: List<KnockUser>,
    var blocks: List<ContentBlockBase>,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf(),
    var id: String,
    var archivedAt: ZonedDateTime? = null,
    var insertedAt: ZonedDateTime? = null,
    var interactedAt: ZonedDateTime? = null,
    var clickedAt: ZonedDateTime? = null,
    var linkClickedAt: ZonedDateTime? = null,
    var readAt: ZonedDateTime? = null,
    var seenAt: ZonedDateTime? = null,
    var tenant: String? = null,
    var totalActivities: Int,
    var totalActors: Int,
    var updatedAt: ZonedDateTime? = null,
)
