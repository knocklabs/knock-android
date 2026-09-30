package app.knock.client.services

import app.knock.client.models.ChannelData

internal class ChannelService: KnockAPIService() {
    suspend fun getUserChannelData(userId: String, channelId: String): ChannelData {
        return get<ChannelData>("/users/${userId.asPathSegment()}/channel_data/${channelId.asPathSegment()}", null)
    }

    suspend fun updateUserChannelData(userId: String, channelId: String, data: Any): ChannelData {
        val body = mapOf(
            "data" to data
        )
        return put<ChannelData>("/users/${userId.asPathSegment()}/channel_data/${channelId.asPathSegment()}", body)
    }
}