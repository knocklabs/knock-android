package app.knock.client

import android.content.Context
import android.content.SharedPreferences
import app.knock.client.models.KnockException
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KnockEnvironment {
    companion object {
        const val defaultBaseUrl: String = "https://api.knock.app"
        const val clientVersion: String = "1.0.3"
    }

    private val previousPushTokensKey = "knock_previous_push_token"

    @Volatile
    private var userId: String? = null

    @Volatile
    private var userToken: String? = null

    @Volatile
    private var publishableKey: String? = null

    @Volatile
    private var pushChannelId: String? = null

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    @Synchronized
    fun getSharedPreferences(): SharedPreferences? = sharedPreferences

    @Synchronized
    @Throws(Exception::class)
    fun getSafeSharedPreferences(): SharedPreferences = getSharedPreferences() ?: throw KnockException.UserIdNotSetError

    @Synchronized
    fun setSharedPreferences(context: Context) {
        val appContext = context.applicationContext
        sharedPreferences = appContext.getSharedPreferences("knock-android", Context.MODE_PRIVATE)
    }

    @Synchronized
    fun getBaseUrl(): String = baseUrl

    @Synchronized
    fun setBaseUrl(baseUrl: String?) {
        this.baseUrl = baseUrl ?: defaultBaseUrl
    }

    @Synchronized
    fun getUserId(): String? = userId

    @Synchronized
    fun setUserId(userId: String?) {
        this.userId = userId
        this.userToken = userToken
    }
    @Synchronized
    @Throws(Exception::class)
    fun getSafeUserId(): String = getUserId() ?: throw KnockException.UserIdNotSetError

    @Synchronized
    fun getUserToken(): String? = userToken

    @Synchronized
    fun setUserToken(userToken: String?) {
        this.userToken = userToken
    }

    @Synchronized
    @Throws(Exception::class)
    fun getSafeUserToken(): String = userToken ?: throw KnockException.UserTokenNotSet

    @Synchronized
    @Throws(Exception::class)
    fun setPublishableKey(key: String?) {
        this.publishableKey = key
    }

    @Synchronized
    fun getPublishableKey(): String = publishableKey!!

    @Synchronized
    @Throws(Exception::class)
    fun getSafePublishableKey(): String = publishableKey ?: throw KnockException.KnockNotSetup

    @Synchronized
    fun setPushChannelId(newChannelId: String?) {
        this.pushChannelId = newChannelId
    }

    @Synchronized
    fun getPushChannelId(): String? = pushChannelId

    @Synchronized
    @Throws(Exception::class)
    fun getSafePushChannelId(): String = pushChannelId ?: throw KnockException.PushChannelIdNotSetError

    fun setDeviceToken(token: String?) {
        val previousTokens = getPreviousPushTokens()
        if (token != null && !previousTokens.contains(token)) {
            setPreviousPushTokens(tokens = previousTokens + token)
        }
    }

    suspend fun getCurrentFcmToken(): String? {
        val token = suspendCoroutine { continuation ->
            // Get the current FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Knock.shared.logError(KnockLogCategory.PUSH_NOTIFICATION, task.exception.toString())
                    continuation.resume(null)
                    return@addOnCompleteListener
                }
                continuation.resume(task.result)
            }
        }
        return token
    }

    private fun setPreviousPushTokens(tokens: List<String>) {
        getSafeSharedPreferences().edit().putStringSet(previousPushTokensKey, tokens.toSet()).apply()
    }

    fun clearPreviousPushTokens() {
        getSafeSharedPreferences().edit().putStringSet(previousPushTokensKey, emptyList<String>().toSet()).apply()
    }

    fun getPreviousPushTokens(): List<String> = getSafeSharedPreferences().getStringSet(previousPushTokensKey, setOf())?.toList() ?: emptyList()
}