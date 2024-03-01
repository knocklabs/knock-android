package app.knock.client.models.feed

import app.knock.client.models.KnockUser
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class KnockActivity(
    var id: String,
    var actor: KnockUser?,
    var recipient: KnockUser?,
    var data: Map<String, Any> = hashMapOf(),
    var insertedAt: ZonedDateTime?,
    var updatedAt: ZonedDateTime?
)
