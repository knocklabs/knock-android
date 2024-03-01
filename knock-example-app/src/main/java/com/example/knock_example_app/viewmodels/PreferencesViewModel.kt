package com.example.knock_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.knock.client.models.preferences.PreferenceSet
import app.knock.client.modules.getUserPreferences
import app.knock.client.Knock
import app.knock.client.models.preferences.ChannelTypeKey
import app.knock.client.models.preferences.ChannelTypePreferenceItem
import app.knock.client.models.preferences.ConditionsArray
import app.knock.client.modules.setUserPreferences
import arrow.core.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PreferencesViewModel: ViewModel() {
    private val _preferenceSet = MutableStateFlow<PreferenceSet?>(null)
    private val _preferenceItems = MutableStateFlow<List<ChannelTypePreferenceItem>?>(null)

    val preferenceSet: StateFlow<PreferenceSet?> = _preferenceSet.asStateFlow()
    val preferenceItems: StateFlow<List<ChannelTypePreferenceItem>?> = _preferenceItems.asStateFlow()

    init {
        loadDefaultUserPreferences()
    }

    private fun loadDefaultUserPreferences() {
        viewModelScope.launch {
            try {
                val preferences = withContext(Dispatchers.IO) {
                    Knock.getUserPreferences("default")
                }
                _preferenceSet.value = preferences
                _preferenceItems.value = preferences.channelTypes.asArrayOfPreferenceItems()
            } catch (e: Exception) {
                Log.e("PreferencesViewModel", "Error in loadDefaultUserPreferences: ${e.message}")
            }
        }
    }

    fun updatePreference(channelTypeKey: ChannelTypeKey, newValue: Either<Boolean, ConditionsArray>) {
        viewModelScope.launch {
            _preferenceSet.value?.let { currentPreferenceSet ->
                val updatedChannelTypes = currentPreferenceSet.channelTypes.copy(
                    email = if (channelTypeKey == ChannelTypeKey.EMAIL) newValue else currentPreferenceSet.channelTypes.email,
                    inAppFeed = if (channelTypeKey == ChannelTypeKey.IN_APP_FEED) newValue else currentPreferenceSet.channelTypes.inAppFeed,
                    sms = if (channelTypeKey == ChannelTypeKey.SMS) newValue else currentPreferenceSet.channelTypes.sms,
                    push = if (channelTypeKey == ChannelTypeKey.PUSH) newValue else currentPreferenceSet.channelTypes.push,
                    chat = if (channelTypeKey == ChannelTypeKey.CHAT) newValue else currentPreferenceSet.channelTypes.chat,
                )

                val updatedPreferenceSet = currentPreferenceSet.copy(channelTypes = updatedChannelTypes)
                _preferenceSet.value = updatedPreferenceSet

                withContext(Dispatchers.IO) {
                    Knock.setUserPreferences(currentPreferenceSet.id, updatedPreferenceSet)
                }
            }
        }
    }
}