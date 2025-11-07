package app.knock.client.modules

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import app.knock.client.Knock
import app.knock.client.Knock.Companion.coroutineScope
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.logError
import app.knock.client.logWarning
import app.knock.client.models.ChannelData
import app.knock.client.models.Device
import app.knock.client.models.KnockException
import app.knock.client.services.ChannelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
internal class ChannelModule {
    private val channelService = ChannelService()
    
    private fun getCurrentLocale(): String {
        return Locale.getDefault().toString().replace('_', '-')
    }
    
    private fun getCurrentTimezone(): String {
        return TimeZone.getDefault().id
    }
    
    private fun createDevice(token: String): Device {
        return Device(
            token = token,
            locale = getCurrentLocale(),
            timezone = getCurrentTimezone()
        )
    }
    
    suspend fun getUserChannelData(channelId: String): ChannelData {
        val userId = Knock.shared.environment.getSafeUserId()
        return channelService.getUserChannelData(userId, channelId)
    }

    suspend fun updateUserChannelData(channelId: String, data: Any): ChannelData {
        val userId = Knock.shared.environment.getSafeUserId()
        return channelService.updateUserChannelData(userId, channelId, data)
    }

    // FCM Device Token Registration
    suspend fun registerTokenForFCM(channelId: String?, token: String): ChannelData {
        Knock.shared.environment.setDeviceToken(token)
        Knock.shared.environment.setPushChannelId(channelId)

        if (!Knock.shared.isAuthenticated() || channelId == null) {
            Knock.shared.logWarning(KnockLogCategory.PUSH_NOTIFICATION, "ChannelId and deviceToken were saved. However, we cannot register for FCM until you have called Knock.signIn().")
            return ChannelData(channelId ?: "", mutableMapOf("devices" to listOf(createDevice(token))))
        }

        return prepareToRegisterTokenOnServer(token, channelId)
    }

    suspend fun unregisterTokenForFCM(channelId: String, token: String): ChannelData {
        try {
            val channelData = getUserChannelData(channelId)
            val previousTokens = Knock.shared.environment.getPreviousPushTokens()

            @Suppress("UNCHECKED_CAST")
            val devices: List<Map<String, Any>> = channelData.data["devices"] as? List<Map<String, Any>> ?: emptyList()

            val updatedDevices = getDeviceDataForServer(token, previousTokens, devices, forUnregistration = true)

            return if (updatedDevices != devices) {
                val newDevices = devices.filter { (it["token"] as? String) != token }
                val data = mapOf("devices" to newDevices)

                val updatedData = updateUserChannelData(channelId, data)
                Knock.shared.environment.clearPreviousPushTokens()

                Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Successful unregisterTokenForFCM()")
                updatedData
            } else {
                Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM()", "Could not unregister user from channel $channelId. Reason: User doesn't have any device tokens associated to the provided channelId.")
                channelData
            }
        } catch (e: KnockException.NetworkError) {
            if (e.code == 404) {
                Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM", "Could not unregister user from channel $channelId. Reason: User doesn't have any channel data associated to the provided channelId.")
                return ChannelData(channelId, mutableMapOf())
            } else {
                Knock.shared.logError(KnockLogCategory.PUSH_NOTIFICATION, "Failed unregisterTokenForFCM", "Could not unregister user from channel $channelId", e)
                throw e
            }
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun getDeviceDataForServer(
        newToken: String,
        previousTokens: List<String>,
        channelDataDevices: List<Map<String, Any>>,
        forUnregistration: Boolean = false
    ): List<Map<String, Any>> {
        val updatedDevices = channelDataDevices.toMutableList()

        // Remove devices with previous tokens
        updatedDevices.removeAll { device ->
            val token = device["token"] as? String
            token != null && previousTokens.contains(token)
        }

        if (forUnregistration) {
            updatedDevices.removeAll { device ->
                (device["token"] as? String) == newToken
            }
        } else {
            // Check if device with this token already exists
            val tokenExists = updatedDevices.any { device ->
                (device["token"] as? String) == newToken
            }
            if (!tokenExists) {
                // Convert Device to Map for the API
                val newDevice = createDevice(newToken)
                updatedDevices.add(mapOf(
                    "token" to newDevice.token,
                    "locale" to newDevice.locale,
                    "timezone" to newDevice.timezone
                ))
            }
        }

        return updatedDevices
    }

    private suspend fun prepareToRegisterTokenOnServer(token: String, channelId: String): ChannelData {
        return try {
            val existingChannelData = getUserChannelData(channelId)

            @Suppress("UNCHECKED_CAST")
            val existingChannelDevices: List<Map<String, Any>> = existingChannelData.data["devices"] as? List<Map<String, Any>> ?: emptyList()

            val previousTokens = Knock.shared.environment.getPreviousPushTokens()

            val preparedDevices = getDeviceDataForServer(token, previousTokens, existingChannelDevices)

            if (preparedDevices != existingChannelDevices) {
                return registerNewDeviceDataOnServer(preparedDevices, channelId)
            } else {
                existingChannelData
            }
        } catch (e: KnockException.UserIdNotSetError) {
            Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Cannot register for FCM until Knock.signIn() is called.")
            return ChannelData(channelId, mutableMapOf("devices" to listOf(createDevice(token))))
        } catch (e: KnockException.NetworkError) {
            if (e.code == 404) {
                val newDevice = createDevice(token)
                return registerNewDeviceDataOnServer(listOf(mapOf(
                    "token" to newDevice.token,
                    "locale" to newDevice.locale,
                    "timezone" to newDevice.timezone
                )), channelId)
            } else {
                throw e
            }
        } catch(e: Exception) {
            throw e
        }
    }

    private suspend fun registerNewDeviceDataOnServer(devices: List<Map<String, Any>>, channelId: String): ChannelData {
        val data = (mapOf("devices" to devices))
        val newChannelData = updateUserChannelData(channelId, data)

        Knock.shared.environment.clearPreviousPushTokens()

        Knock.shared.logDebug(KnockLogCategory.PUSH_NOTIFICATION, "Device token registered on server")
        return newChannelData
    }
}

suspend fun Knock.getUserChannelData(channelId: String): ChannelData {
    return channelModule.getUserChannelData(channelId)
}
@Suppress("unused")
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

@Suppress("unused")
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

@Suppress("unused")
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

@Suppress("unused")
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

@Suppress("unused")
fun Knock.requestNotificationPermission(activity: Activity, requestCode: Int = 1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCode)
    }
}

@Suppress("unused")
fun Knock.isPushPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
