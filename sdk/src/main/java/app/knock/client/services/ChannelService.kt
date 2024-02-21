package app.knock.client.services

import app.knock.client.Knock
import app.knock.client.KnockLogger
import app.knock.client.log
import app.knock.client.models.ChannelData

class ChannelService: KnockAPIService() {
    suspend fun getUserChannelData(userId: String, channelId: String): ChannelData {
        val data = get<ChannelData>("/users/$userId/channel_data/$channelId", null)
//        return try {
//            val data = get<ChannelData>("/users/$userId/channel_data/$channelId", null)
//            // Log success, adjust as per your project
////            println("DEBUG: getUserChannelData successful for channelId: $channelId")
////            data
//        } catch (error: Exception) {
//            // Log error, adjust as per your project
//            println("ERROR: getUserChannelData failed for channelId: $channelId with error: ${error.localizedMessage}")
//            Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.CHANNEL, "getUserChannelData failed for channelId: $channelId ")
//            throw error
//        }
    }

    suspend fun updateUserChannelData(userId: String, channelId: String, data): ChannelData {
        val data = get<ChannelData>("/users/$userId/channel_data/$channelId", null)
//        return try {
//            val data = get<ChannelData>("/users/$userId/channel_data/$channelId", null)
//            // Log success, adjust as per your project
////            println("DEBUG: getUserChannelData successful for channelId: $channelId")
////            data
//        } catch (error: Exception) {
//            // Log error, adjust as per your project
//            println("ERROR: getUserChannelData failed for channelId: $channelId with error: ${error.localizedMessage}")
//            Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.CHANNEL, "getUserChannelData failed for channelId: $channelId ")
//            throw error
//        }
    }

    /**
     * Gets the channel data for the current user.
     *
     * @param channelId the id of the channel
     * @param data the shape of the payload varies depending on the channel. You can learn more about channel data schemas [here](https://docs.knock.app/send-notifications/setting-channel-data#provider-data-requirements).
     */
    fun updateUserChannelData(channelId: String, data: Any, completionHandler: (Result<ChannelData>) -> Unit) {
        val body = mapOf(
            "data" to data
        )
        put<ChannelData>("/users/$userId/channel_data/$channelId", body)
    }
}