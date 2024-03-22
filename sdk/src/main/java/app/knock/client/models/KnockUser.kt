package app.knock.client.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

@JsonIgnoreProperties("__typename", "created_at", "updated_at")
data class KnockUser(
    var id: String,
    var name: String?,
    var email: String?,
    var avatar: String?,
    var phoneNumber: String?,

    @JsonAnySetter
    @get:JsonAnyGetter
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var properties: MutableMap<String, Any> = hashMapOf(),
)
