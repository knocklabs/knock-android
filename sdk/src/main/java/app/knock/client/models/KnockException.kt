package app.knock.client.models

import okhttp3.Request
import okhttp3.Response

sealed class KnockException : Exception() {
    data class RuntimeError(val descriptionString: String) : KnockException()
    data class DecodingError(val type: String): KnockException()
    data object UserIdNotSetError : KnockException()
    data object UserTokenNotSet : KnockException()
    data object DevicePushTokenNotSet : KnockException()
    data object PushChannelIdNotSetError : KnockException()
    data object KnockNotSetup : KnockException()
    data object WrongKeyError : KnockException()

    data class NetworkError(
        val title: String = "Error",
        val code: Int,
        val description: String,
        val request: Request? = null,
        val response: Response? = null
    ): KnockException()

    override val message: String?
        get() = when (this) {
            is RuntimeError -> descriptionString
            is DecodingError -> "Failed to decode: $type"
            is NetworkError -> "$title | code: $code | description: $description"
            UserIdNotSetError -> "UserId not found. Please authenticate your userId with Knock.shared.signIn()."
            UserTokenNotSet -> "User token must be set for production environments. Please authenticate your user token with Knock.shared.signIn()."
            PushChannelIdNotSetError -> "PushChannelId not found. Please setup with Knock.shared.setup() or Knock.shared.registerTokenForAPNS()."
            DevicePushTokenNotSet -> "Device Push Notification token not found. Please setup with Knock.shared.registerTokenForAPNS()."
            KnockNotSetup -> "Knock instance still needs to be setup. Please setup with Knock.shared.setup()."
            WrongKeyError -> "You are using your secret API key on the client. Please use the public key."
        }
}