package app.knock.client.modules

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.logError
import app.knock.client.logWarning
import app.knock.client.models.ChannelData
import app.knock.client.models.KnockException
import app.knock.client.services.ChannelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
internal class ChannelModule {
    private val channelService = ChannelService()
    suspend fun getUserChannelData(channelId: String): ChannelData {
        val userId = Knock.environment.getSafeUserId()
        return channelService.getUserChannelData(userId, channelId)
    }

    suspend fun updateUserChannelData(channelId: String, data: Any): ChannelData {
        val userId = Knock.environment.getSafeUserId()
        return channelService.updateUserChannelData(userId, channelId, data)
    }

    // FCM Device Token Registration
    suspend fun registerTokenForFCM(channelId: String?, token: String): ChannelData {
        Knock.environment.setDeviceToken(token)
        Knock.environment.setPushChannelId(channelId)

        if (!Knock.isAuthenticated() || channelId == null) {
            Knock.logWarning(KnockLogCategory.PUSH_NOTIFICATION, "ChannelId and deviceToken were saved. However, we cannot register for FCM until you have called Knock.signIn().")
            return ChannelData(channelId ?: "", mutableMapOf("tokens" to listOf(token)))
        }

        return prepareToRegisterTokenOnServer(token, channelId)
    }

    suspend fun unregisterTokenForFCM(channelId: String, token: String): ChannelData {
        try {
            val channelData = getUserChannelData(channelId)
            val previousTokens = Knock.environment.getPreviousPushTokens()

            @Suppress("UNCHECKED_CAST")
            val tokens: List<String> = channelData.data["tokens"] as? List<String> ?: emptyList()

            val updatedTokens = getTokenDataForServer(token, previousTokens, tokens, forUnregistration = true)

            return if (updatedTokens != tokens) {
                val newTokens = tokens.toSet().minus(token).toList()
                val data = mapOf("tokens" to newTokens)

                val updatedData = updateUserChannelData(channelId, data)
                Knock.environment.clearPreviousPushTokens()

                Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Successful unregisterTokenForFCM()")
                updatedData
            } else {
                Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM()", "Could not unregister user from channel $channelId. Reason: User doesn't have any device tokens associated to the provided channelId.")
                channelData
            }
        } catch (e: KnockException.NetworkError) {
            if (e.code == 404) {
                Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM", "Could not unregister user from channel $channelId. Reason: User doesn't have any channel data associated to the provided channelId.")
                return ChannelData(channelId, mutableMapOf())
            } else {
                Knock.logError(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM", "Could not unregister user from channel $channelId", e)
                throw e
            }
        }
    }

    private fun getTokenDataForServer(
        newToken: String,
        previousTokens: List<String>,
        channelDataTokens: List<String>,
        forUnregistration: Boolean = false
    ): List<String> {
        val updatedTokens = channelDataTokens.toMutableList()

        updatedTokens.removeAll(previousTokens)

        if (forUnregistration) {
            updatedTokens.remove(newToken)
        } else if (!updatedTokens.contains(newToken)) {
            updatedTokens.add(newToken)
        }

        return updatedTokens
    }

    private suspend fun prepareToRegisterTokenOnServer(token: String, channelId: String): ChannelData {
        return try {
            val existingChannelData = getUserChannelData(channelId)

            @Suppress("UNCHECKED_CAST")
            val existingChannelTokens: List<String> = existingChannelData.data["tokens"] as? List<String> ?: emptyList()

            val previousTokens = Knock.environment.getPreviousPushTokens()

            val preparedTokens = getTokenDataForServer(token, previousTokens, existingChannelTokens)

            if (preparedTokens != existingChannelTokens) {
                return registerNewTokenDataOnServer(preparedTokens, channelId)
            } else {
                existingChannelData
            }
        } catch (e: KnockException.UserIdNotSetError) {
            Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Cannot register for FCM until Knock.signIn() is called.")
            return ChannelData(channelId, mutableMapOf("tokens" to listOf(token)))
        } catch (e: KnockException.NetworkError) {
            if (e.code == 404) {
                return registerNewTokenDataOnServer(listOf(token), channelId)
            } else {
                throw e
            }
        } catch(e: Exception) {
            throw e
        }
    }

    private suspend fun registerNewTokenDataOnServer(tokens: List<String>, channelId: String): ChannelData {
        val data = (mapOf("tokens" to tokens))
        val newChannelData = updateUserChannelData(channelId, data)

        Knock.environment.clearPreviousPushTokens()

        Knock.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Device token registered on server")
        return newChannelData
    }
}

suspend fun Knock.getUserChannelData(channelId: String): ChannelData {
    return channelModule.getUserChannelData(channelId)
}

fun Knock.getUserChannelData(channelId: String, completionHandler: (Result<ChannelData>) -> Unit) = coroutineScope.launch(
    Dispatchers.Main) {
    try {
        val channel = withContext(Dispatchers.IO) {
            getUserChannelData(channelId)
        }
        completionHandler(Result.success(channel))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}

suspend fun Knock.updateUserChannelData(channelId: String, data: Any): ChannelData {
    return channelModule.updateUserChannelData(channelId, data)
}

fun Knock.updateUserChannelData(channelId: String, data: Any, completionHandler: (Result<ChannelData>) -> Unit) = coroutineScope.launch(
    Dispatchers.Main) {
    try {
        val channel = withContext(Dispatchers.IO) {
            updateUserChannelData(channelId, data)
        }
        completionHandler(Result.success(channel))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}

suspend fun Knock.getCurrentDeviceToken(): String? {
    return environment.getCurrentFcmToken()
}

fun Knock.getCurrentDeviceToken(completionHandler: (String?) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    val token = withContext(Dispatchers.IO) {
        getCurrentDeviceToken()
    }
    completionHandler(token)
}

/**
 * Registers an Apple Push Notification Service token so that the device can receive remote push notifications.
 * This is a convenience method that internally gets the channel data and searches for the token.
 * If it exists, then it's already registered and it returns. If the data does not exists or the token is missing from the array, it's added.
 *
 * You can learn more about FCM [here](https://firebase.google.com/docs/cloud-messaging/android/client).
 *
 * **Attention:** There's a race condition because the getting/setting of the token are not made in a transaction.
 *
 * @param channelId the id of the APNS channel
 * @param token the FCM device token as a `String`
 */
suspend fun Knock.registerTokenForFCM(channelId: String, token: String): ChannelData {
    return channelModule.registerTokenForFCM(channelId, token)
}

fun Knock.registerTokenForFCM(channelId: String, token: String, completionHandler: (Result<ChannelData>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val channel = withContext(Dispatchers.IO) {
            registerTokenForFCM(channelId, token)
        }
        completionHandler(Result.success(channel))
    } catch(e: Exception) {
        completionHandler(Result.failure(e))
    }
}

suspend fun Knock.unregisterTokenForFCM(channelId: String, token: String): ChannelData {
    return channelModule.unregisterTokenForFCM(channelId, token)
}

fun Knock.unregisterTokenForFCM(channelId: String, token: String, completionHandler: (Result<ChannelData>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val channel = withContext(Dispatchers.IO) {
            unregisterTokenForFCM(channelId, token)
        }
        completionHandler(Result.success(channel))
    } catch(e: Exception) {
        completionHandler(Result.failure(e))
    }
}

fun Knock.requestNotificationPermission(activity: Activity, requestCode: Int = 1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCode)
    }
}

fun Knock.isPushPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}