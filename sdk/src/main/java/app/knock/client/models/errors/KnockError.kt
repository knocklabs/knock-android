package app.knock.client.models.errors

sealed class KnockError : Exception() {
    data class RuntimeError(val descriptionString: String) : KnockError()
    data object UserIdNotSetError : KnockError()
    data object UserTokenNotSet : KnockError()
    data object DevicePushTokenNotSet : KnockError()
    data object PushChannelIdNotSetError : KnockError()
    data object KnockNotSetup : KnockError()
    data object WrongKeyError : KnockError()

    override val message: String?
        get() = when (this) {
            is RuntimeError -> descriptionString
            UserIdNotSetError -> "UserId not found. Please authenticate your userId with Knock.shared.signIn()."
            UserTokenNotSet -> "User token must be set for production environments. Please authenticate your user token with Knock.shared.signIn()."
            PushChannelIdNotSetError -> "PushChannelId not found. Please setup with Knock.shared.setup() or Knock.shared.registerTokenForAPNS()."
            DevicePushTokenNotSet -> "Device Push Notification token not found. Please setup with Knock.shared.registerTokenForAPNS()."
            KnockNotSetup -> "Knock instance still needs to be setup. Please setup with Knock.shared.setup()."
            WrongKeyError -> "You are using your secret API key on the client. Please use the public key."
        }
}