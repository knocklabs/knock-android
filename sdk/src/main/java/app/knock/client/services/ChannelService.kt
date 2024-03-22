package app.knock.client.services

import app.knock.client.models.ChannelData

internal class ChannelService: KnockAPIService() {
    suspend fun getUserChannelData(userId: String, channelId: String): ChannelData {
        return get<ChannelData>("/users/$userId/channel_data/$channelId", null)
    }

    suspend fun updateUserChannelData(userId: String, channelId: String, data: Any): ChannelData {
        val body = mapOf(
            "data" to data
        )
        return put<ChannelData>("/users/$userId/channel_data/$channelId", body)
    }
}