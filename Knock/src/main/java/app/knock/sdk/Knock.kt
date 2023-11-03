package app.knock.sdk

/**
 * Returns a new instance of the Knock Client
 *
 * @param publishableKey your public API key
 * @param userId the user-id that will be used in the subsequent method calls
 * @param userToken (optional) user token. Used in production when enhanced security is enabled
 * @param hostname (optional) custom hostname of the API, including schema (https://)
 */
class Knock(
    var publishableKey: String,
    var userId: String,
    var userToken: String? = null,
    hostname: String? = null
) {
    var api: KnockAPI

    init {
        if (publishableKey.startsWith("sk_")) {
            throw Exception("[Knock] You are using your secret API key on the client. Please use the public key.")
        }
        this.api = KnockAPI(publishableKey, userToken, hostname)
    }
}