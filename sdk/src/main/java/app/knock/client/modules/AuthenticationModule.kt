package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.Knock.Companion.coroutineScope
import app.knock.client.KnockLogCategory
import app.knock.client.logWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class AuthenticationModule {

    suspend fun signIn(userId: String, userToken: String?) {
        Knock.shared.environment.setUserId(userId)
        Knock.shared.environment.setUserToken(userToken)

        val token = Knock.shared.getCurrentDeviceToken()
        val channelId = Knock.shared.environment.getPushChannelId()
        if (token != null && channelId != null) {
            try {
                Knock.shared.channelModule.registerTokenForFCM(channelId, token)
            } catch (e: Exception) {
                Knock.shared.logWarning(KnockLogCategory.USER, "signIn", "Successfully set user, however, unable to registerTokenForAPNS at this time.")
            }
        }
    }

    suspend fun signOut() {
        val channelId = Knock.shared.environment.getPushChannelId()
        val token = Knock.shared.getCurrentDeviceToken()
        if (channelId != null && token != null) {
            try {
                Knock.shared.channelModule.unregisterTokenForFCM(channelId, token)
            } finally {
                clearDataForSignOut()
            }
        } else {
            clearDataForSignOut()
        }
    }

    private fun clearDataForSignOut() {
        Knock.shared.environment.setUserId(null)
        Knock.shared.environment.setUserToken(null)
    }
}

/**
 * Convenience method to determine if a user is currently authenticated for the Knock instance.
 */
fun Knock.isAuthenticated(checkUserToken: Boolean = false): Boolean {
    val isUser = environment.getUserId()?.isEmpty() == false
    if (checkUserToken) {
        val hasToken = environment.getUserToken()?.isEmpty() == false
        return isUser && hasToken
    }
    return isUser
}

/**
 * Sets the userId and userToken for the current Knock instance.
 * If the device token and pushChannelId were set previously, this will also attempt to register the token to the user that is being signed in.
 * This does not get the user from the database nor does it return the full User object.
 * You should consider using this in areas where you update your local user's state.
 *
 * @param userId: The id of the Knock channel to lookup.
 * @param userToken: (optional) The id of the Knock channel to lookup.
 */
suspend fun Knock.signIn(userId: String, userToken: String?) {
    authenticationModule.signIn(userId, userToken)
}

@Suppress("unused")
fun Knock.signIn(userId: String, userToken: String?, completionHandler: (Result<Unit>) -> Unit)= coroutineScope.launch(Dispatchers.Main) {
    try {
        signIn(userId, userToken)
        completionHandler(Result.success(Unit))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}

suspend fun Knock.signOut() {
    authenticationModule.signOut()
}

/**
 * Sets the userId and userToken for the current Knock instance back to nil.
 * If the device token and pushChannelId were set previously, this will also attempt to unregister the token to the user that is being signed out so they don't receive pushes they shouldn't get.
 * You should call this when your user signs out
 * NOTE: This will not clear the device token so that it can be accessed for the next user to login.
 */
@Suppress("unused")
fun Knock.signOut(completionHandler: (Result<Unit>) -> Void)= coroutineScope.launch(Dispatchers.Main) {
    try {
        signOut()
        completionHandler(Result.success(Unit))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}
