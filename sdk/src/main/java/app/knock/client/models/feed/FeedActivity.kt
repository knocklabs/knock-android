package app.knock.client.models.feed

import app.knock.client.models.KnockUser
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedActivity(
    var id: String,
    var actor: KnockUser?,
    var recipient: KnockUser?,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf(),
    var insertedAt: ZonedDateTime?,
    var updatedAt: ZonedDateTime?
)
