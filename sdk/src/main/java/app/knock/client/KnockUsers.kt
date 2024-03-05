package app.knock.client

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

// Users

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

fun Knock.getUser(completionHandler: (Result<KnockUser>) -> Unit) {
    api.decodeFromGet("/users/$userId", null, completionHandler)
}

fun Knock.updateUser(user: KnockUser, completionHandler: (Result<KnockUser>) -> Unit) {
    api.decodeFromPut("/users/$userId", user, completionHandler)
}
