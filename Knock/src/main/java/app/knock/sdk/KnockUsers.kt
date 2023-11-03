package app.knock.sdk

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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
    var properties: MutableMap<String, Any> = hashMapOf(),
)

fun Knock.getUser(completionHandler: (Result<KnockUser>) -> Unit) {
    api.decodeFromGet<KnockUser>("/users/$userId", null, completionHandler)
}

fun Knock.updateUser(user: KnockUser, completionHandler: (Result<KnockUser>) -> Unit) {
    api.decodeFromPut<KnockUser>("/users/$userId", user, completionHandler)
}
