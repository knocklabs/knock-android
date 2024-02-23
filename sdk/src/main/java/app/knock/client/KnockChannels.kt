package app.knock.client


//
///**
// * Registers an Apple Push Notification Service token so that the device can receive remote push notifications. This is a convenience method that internally gets the channel data and searches for the token. If it exists, then it's already registered and it returns. If the data does not exists or the token is missing from the array, it's added.
// *
// * You can learn more about FCM [here](https://firebase.google.com/docs/cloud-messaging/android/client).
// *
// * **Attention:** There's a race condition because the getting/setting of the token are not made in a transaction.
// *
// * @param channelId the id of the APNS channel
// * @param token the FCM device token as a `String`
// */
//fun Knock.registerTokenForFCM(channelId: String, token: String, completionHandler: (Result<ChannelData>) -> Unit) {
//    getUserChannelData(channelId) { result ->
//        result.fold(
//            onFailure = {
//                // there's no data registered on that channel for that user, we'll create a new record
//                val data = mapOf(
//                    "tokens" to listOf(
//                        token
//                    )
//                )
//                updateUserChannelData(channelId, data, completionHandler)
//            },
//            onSuccess = { channelData ->
//                if (channelData.data.isEmpty() || channelData.data["tokens"] == null) {
//                    // we don't have data for that channel for that user or an array of tokens, we'll create a new record
//                    val data = mapOf(
//                        "tokens" to listOf(
//                            token
//                        )
//                    )
//                    updateUserChannelData(channelId, data, completionHandler)
//                    return@fold
//                }
//
//                try {
//                    val tokens: List<String> = channelData.data["tokens"] as List<String>
//
//                    if (tokens.contains(token)) {
//                        // we already have the token registered
//                        completionHandler(Result.success(channelData))
//                    }
//                    else {
//                        // we need to register the token
//                        channelData.data["tokens"] = tokens.toMutableList().add(token)
//                        updateUserChannelData(channelId, channelData, completionHandler)
//                        return@fold
//                    }
//                }
//                catch (e: Exception) {
//                    completionHandler(Result.failure(Exception("incorrect type of channelData.data[\"tokens\"], not a List<String>")))
//                    return@fold
//                }
//            }
//        )
//    }
//}