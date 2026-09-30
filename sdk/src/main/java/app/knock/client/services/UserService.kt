package app.knock.client.services

import app.knock.client.models.KnockUser

internal class UserService: KnockAPIService() {
    suspend fun getUser(userId: String): KnockUser {
        return get("/users/${userId.asPathSegment()}", null)
    }

    suspend fun updateUser(user: KnockUser): KnockUser {
        return put("/users/${user.id.asPathSegment()}", user)
    }
}