package app.knock.client

class KnockEnvironment {
    companion object {
        val defaultBaseUrl: String = "https://api.knock.app"
        val appVersion: String = "1.0.0"
    }

    private val userDevicePushTokenKey = "knock_push_device_token"
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

    @Synchronized
    fun getBaseUrl(): String = baseUrl

    @Synchronized
    fun setBaseUrl(baseUrl: String?) {
        this.baseUrl = baseUrl ?: defaultBaseUrl
    }

    @Synchronized
    fun setUserInfo(userId: String?, userToken: String?) {
        this.userId = userId
        this.userToken = userToken
    }

    @Synchronized
    fun getUserId(): String? = userId

    @Synchronized
    @Throws(Exception::class)
    fun getSafeUserId(): String = userId ?: throw Exception("UserID not set")

    @Synchronized
    fun getUserToken(): String? = userToken

    @Synchronized
    @Throws(Exception::class)
    fun getSafeUserToken(): String = userToken ?: throw Exception("User token not set")

    @Synchronized
    @Throws(Exception::class)
    fun setPublishableKey(key: String) {
        if (key.startsWith("sk_")) {
            throw Exception("Wrong key error")
        }
        this.publishableKey = key
    }

    @Synchronized
    fun getPublishableKey(): String = publishableKey!!

    @Synchronized
    @Throws(Exception::class)
    fun getSafePublishableKey(): String = publishableKey ?: throw Exception("Knock not setup")

    @Synchronized
    fun setPushChannelId(newChannelId: String?) {
        this.pushChannelId = newChannelId
    }

    @Synchronized
    fun getPushChannelId(): String? = pushChannelId

    @Synchronized
    @Throws(Exception::class)
    fun getSafePushChannelId(): String = pushChannelId ?: throw Exception("PushChannelID not set")

    suspend fun setDeviceToken(token: String?) {
        val previousTokens = getPreviousPushTokens()
        if (token != null && !previousTokens.contains(token)) {
            setPreviousPushTokens(tokens = previousTokens + token)
        }
        Knock.getSharedPreferences().edit().putString(userDevicePushTokenKey, token).apply()
    }

    fun getDeviceToken(): String? = Knock.getSharedPreferences().getString(userDevicePushTokenKey, null)

    @Throws(Exception::class)
    fun getSafeDeviceToken(): String = getDeviceToken() ?: throw Exception("Device push token not set")

    private fun setPreviousPushTokens(tokens: List<String>) {
        Knock.getSharedPreferences().edit().putStringSet(previousPushTokensKey, tokens.toSet()).apply()
    }

    fun getPreviousPushTokens(): List<String> = Knock.getSharedPreferences().getStringSet(previousPushTokensKey, setOf())?.toList() ?: emptyList()
}
