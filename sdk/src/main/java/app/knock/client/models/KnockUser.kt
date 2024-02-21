package app.knock.client.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties("__typename", "created_at", "updated_at")
data class KnockUser(
    var id: String,
    var name: String?,
    var email: String?,

    var avatar: String?,
    var phoneNumber: String?,

    @JsonAnySetter
    @get:JsonAnyGetter
    var properties: MutableMap<String, Any> = hashMapOf(),
)
