package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AuthenticationModule {

    suspend fun signIn(userId: String, userToken: String?) {
        withContext(Dispatchers.IO) {
            Knock.environment.setUserInfo(userId, userToken)

            val token = Knock.environment.getDeviceToken()
            val channelId = Knock.environment.getPushChannelId()
            if (token != null && channelId != null) {
                try {
                    Knock.channelModule.registerTokenForAPNS(channelId, token)
                } catch (e: Exception) {
                    Knock.logger.log(KnockLogger.LogType.WARNING, KnockLogger.LogCategory.USER, "signIn", "Successfully set user, however, unable to registerTokenForAPNS at this time.")
                }
            }
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            val channelId = Knock.environment.getPushChannelId()
            val token = Knock.environment.getDeviceToken()
            if (channelId != null && token != null) {
                try {
                    Knock.channelModule.unregisterTokenForAPNS(channelId, token)
                } finally {
                    clearDataForSignOut()
                }
            } else {
                clearDataForSignOut()
            }
        }
    }

    private suspend fun clearDataForSignOut() {
        Knock.environment.setUserInfo(null, null)
    }
}

suspend fun Knock.signOut(userId: String, userToken: String?) {
    Knock.authenticationModule.signIn(userId, userToken)
}

fun Knock.signOut(userId: String, userToken: String?, completion: (Result<Unit>) -> Void) {
    coroutineScope.launch {
        try {
            coroutineScope.launch(Dispatchers.Main) {
                completion(Result.success(Unit))
            }
        } catch (e: Exception) {
            coroutineScope.launch(Dispatchers.Main) {
                completion(Result.failure(e))
            }
        }
    }
}
