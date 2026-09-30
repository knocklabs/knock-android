package app.knock.client.services

import app.knock.client.models.preferences.PreferenceSet

internal class PreferenceService: KnockAPIService() {
    suspend fun getAllUserPreferences(userId: String): List<PreferenceSet> {
        return get<List<PreferenceSet>>("/users/${userId.asPathSegment()}/preferences", null)
    }

    suspend fun getUserPreferences(userId: String, preferenceId: String): PreferenceSet {
        return get<PreferenceSet>("/users/${userId.asPathSegment()}/preferences/${preferenceId.asPathSegment()}", null)
    }

    suspend fun setUserPreferences(userId: String, preferenceId: String, preferenceSet: PreferenceSet): PreferenceSet {
        return put<PreferenceSet>("/users/${userId.asPathSegment()}/preferences/${preferenceId.asPathSegment()}", preferenceSet)
    }
}