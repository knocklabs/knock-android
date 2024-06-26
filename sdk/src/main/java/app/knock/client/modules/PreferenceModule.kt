package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.Knock.Companion.coroutineScope
import app.knock.client.models.preferences.PreferenceSet
import app.knock.client.services.PreferenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PreferenceModule {
    private val preferenceService = PreferenceService() // Assume this exists

    suspend fun getAllUserPreferences(): List<PreferenceSet> {
        val userId = Knock.shared.environment.getSafeUserId()
        return preferenceService.getAllUserPreferences(userId)
    }

    suspend fun getUserPreferences(preferenceId: String): PreferenceSet {
        val userId = Knock.shared.environment.getSafeUserId()
        return preferenceService.getUserPreferences(userId, preferenceId)
    }

    suspend fun setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet {
        val userId = Knock.shared.environment.getSafeUserId()
        return preferenceService.setUserPreferences(userId, preferenceId, preferenceSet)
    }
}

/**
 * Retrieve all user's preference sets. Will always return an empty preference set object, even if it does not currently exist for the user.
 * https://docs.knock.app/reference#get-preferences-user#get-preferences-user
 */
suspend fun Knock.getAllUserPreferences(): List<PreferenceSet> {
    return preferenceModule.getAllUserPreferences()
}

@Suppress("unused")
fun Knock.getAllUserPreferences(completionHandler: (Result<List<PreferenceSet>>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val preferences = withContext(Dispatchers.IO) {
            getAllUserPreferences()
        }
        completionHandler(Result.success(preferences))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}

/**
 * Retrieve a user's preference set. Will always return an empty preference set object, even if it does not currently exist for the user.
 * https://docs.knock.app/reference#get-preferences-user#get-preferences-user
 *
 * @param preferenceId: The preferenceId for the PreferenceSet.
 */
suspend fun Knock.getUserPreferences(preferenceId: String): PreferenceSet {
    return preferenceModule.getUserPreferences(preferenceId)
}

@Suppress("unused")
fun Knock.getUserPreferences(preferenceId: String, completionHandler: (Result<PreferenceSet>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val preferences = withContext(Dispatchers.IO) {
            getUserPreferences(preferenceId)
        }
        completionHandler(Result.success(preferences))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}

/**
 * Sets preferences within the given preference set. This is a destructive operation and will replace any existing preferences with the preferences given.
 *
 * If no user exists in the current environment for the current user, Knock will create the user entry as part of this request.
 *
 * The preference set :id can be either "default" or a tenant.id. Learn more about per-tenant preference sets in our preferences guide.
 * https://docs.knock.app/send-and-manage-data/preferences#preference-sets
 * https://docs.knock.app/reference#get-preferences-user#set-preferences-user
 *
 * @param preferenceId: The preferenceId for the PreferenceSet.
 * @param preferenceSet: PreferenceSet with updated properties.
 */
suspend fun Knock.setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet {
    return preferenceModule.setUserPreferences(preferenceId, preferenceSet)
}

@Suppress("unused")
fun Knock.setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet, completionHandler: (Result<PreferenceSet>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val preferences = withContext(Dispatchers.IO) {
            setUserPreferences(preferenceId, preferenceSet)
        }
        completionHandler(Result.success(preferences))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}