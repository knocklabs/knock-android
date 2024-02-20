package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockLogger
import app.knock.client.PreferenceSet
import app.knock.client.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PreferenceModule {
    private val preferenceService = PreferenceService() // Assume this exists

    suspend fun getAllUserPreferences(): List<PreferenceSet> = withContext(Dispatchers.IO) {
        try {
            val userId = Knock.environment.getSafeUserId() // Assume this exists
            val set = preferenceService.getAllUserPreferences(userId) // Assume this is a suspend function
            Knock.log(KnockLogger.LogType.DEBUG, KnockLogger.LogCategory.PREFERENCES, "getAllUserPreferences", status = KnockLogger.LogStatus.SUCCESS)
            set
        } catch (error: Exception) {
            Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.PREFERENCES, "getAllUserPreferences", errorMessage = error.localizedMessage, status = KnockLogger.LogStatus.FAIL)
            throw error
        }
    }

    suspend fun getUserPreferences(preferenceId: String): PreferenceSet = withContext(Dispatchers.IO) {
        try {
            val userId = Knock.environment.getSafeUserId() // Assume this exists
            val set = preferenceService.getUserPreferences(userId, preferenceId) // Assume this is a suspend function
            Knock.log(KnockLogger.LogType.DEBUG, KnockLogger.LogCategory.PREFERENCES, "getUserPreferences", status = KnockLogger.LogStatus.SUCCESS)
            set
        } catch (error: Exception) {
            Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.PREFERENCES, "getUserPreferences", errorMessage = error.localizedMessage, status = KnockLogger.LogStatus.FAIL)
            throw error
        }
    }

    suspend fun setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet = withContext(Dispatchers.IO) {
        try {
            val userId = Knock.environment.getSafeUserId() // Assume this exists
            val set = preferenceService.setUserPreferences(userId, preferenceId, preferenceSet) // Assume this is a suspend function
            Knock.log(KnockLogger.LogType.DEBUG, KnockLogger.LogCategory.PREFERENCES, "setUserPreferences", status = KnockLogger.LogStatus.SUCCESS)
            set
        } catch (error: Exception) {
            Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.PREFERENCES, "setUserPreferences", errorMessage = error.localizedMessage, status = KnockLogger.LogStatus.FAIL)
            throw error
        }
    }
}

suspend fun Knock.getAllUserPreferences(): List<PreferenceSet> {
    return preferenceModule.getAllUserPreferences()
}

fun Knock.getAllUserPreferences(completionHandler: (Result<List<PreferenceSet>>) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val preferences = withContext(Dispatchers.IO) { getAllUserPreferences() }
            completionHandler(Result.success(preferences))
        } catch (e: Exception) {
            completionHandler(Result.failure(e))
        }
    }
}

suspend fun Knock.getUserPreferences(preferenceId: String): PreferenceSet {
    return preferenceModule.getUserPreferences(preferenceId)
}

suspend fun Knock.setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet {
    return preferenceModule.setUserPreferences(preferenceId, preferenceSet)
}