package app.knock.client.services

import app.knock.client.models.preferences.PreferenceSet

internal class PreferenceService: KnockAPIService() {
    suspend fun getAllUserPreferences(userId: String): List<PreferenceSet> {
        return get<List<PreferenceSet>>("/users/$userId/preferences", null)
    }

    suspend fun getUserPreferences(userId: String, preferenceId: String): PreferenceSet {
        return get<PreferenceSet>("/users/$userId/preferences/$preferenceId", null)
    }

    suspend fun setUserPreferences(userId: String, preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet {
        return put<PreferenceSet>("/users/$userId/preferences/$preferenceId", preferenceSet)
    }
}