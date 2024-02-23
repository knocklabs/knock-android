package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.models.KnockUser
import app.knock.client.services.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

internal class UserModule {
    private val userService = UserService()

    suspend fun getUser(): KnockUser {
        val userId = Knock.environment.getSafeUserId()
        return userService.getUser(userId)
    }

    suspend fun updateUser(user: KnockUser): KnockUser {
        return userService.updateUser(user)
    }
}

/**
 * Returns the userId that was set from the Knock.shared.signIn method.
 */
fun Knock.getUserId(): String? {
    return environment.getUserId()
}

/**
 * Retrieve the current user, including all properties previously set.
 * https://docs.knock.app/reference#get-user#get-user
 */
suspend fun Knock.getUser(): KnockUser {
    return userModule.getUser()
}

fun Knock.getUser(completionHandler: (Result<KnockUser>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val user = getUser()
        completionHandler(Result.success(user))
    } catch(e: Exception) {
        completionHandler(Result.failure(e))
    }
}

/**
 * Updates the current user and returns the updated User result.
 */
suspend fun Knock.updateUser(user: KnockUser): KnockUser {
    return userModule.updateUser(user)
}

fun Knock.updateUser(user: KnockUser, completionHandler: (Result<KnockUser>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val newUser = updateUser(user)
        completionHandler(Result.success(newUser))
    } catch(e: Exception) {
        completionHandler(Result.failure(e))
    }
}