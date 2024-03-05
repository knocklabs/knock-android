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
    var activities: List<FeedActivity>?,
    var actors: List<KnockUser>,
    var blocks: List<Block>,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf(),
    var id: String,
    var insertedAt: ZonedDateTime?,
    var interactedAt: ZonedDateTime?,
    var clickedAt: ZonedDateTime?,
    var linkClickedAt: ZonedDateTime?,
    var readAt: ZonedDateTime?,
    var seenAt: ZonedDateTime?,
    var tenant: String?,
    var totalActivities: Int,
    var totalActors: Int,
    var updatedAt: ZonedDateTime?,
)
