package com.example.knock_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.knock.client.Knock
import app.knock.client.models.feed.BulkChannelMessageStatusUpdateType
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatusBatchUpdateType
import app.knock.client.modules.batchUpdateStatuses
import com.example.knock_example_app.Team
import com.example.knock_example_app.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppFeedViewModel : ViewModel() {
    private val _feed = MutableStateFlow<Feed?>(null)
    val feed: StateFlow<Feed?> = _feed.asStateFlow()
    var selectedTeam: Team = Utils.teams.first()

    private val loggerTag = "InAppFeedViewModel"

    fun feedEntries(): List<FeedItem> {
        return feed.value?.entries ?: listOf()
    }

    fun initializeFeed() {
        viewModelScope.launch {
            try {
                val userFeed = Knock.feedManager?.getUserFeedContent(FeedClientOptions())
                userFeed?.let {
                    _feed.value = it
                    _feed.value?.pageInfo?.before = it.entries.firstOrNull()?.feedCursor
                }

                Knock.feedManager?.connectToFeed()

                Knock.feedManager?.on("new-message") {
                    viewModelScope.launch {
                        val feedOptions = FeedClientOptions(tenant = selectedTeam.id, hasTenant = true, before = feed.value?.pageInfo?.before)
                        val result = Knock.feedManager?.getUserFeedContent(feedOptions)

                        withContext(Dispatchers.Main) {
                            result?.let { feedResult ->
                                _feed.value?.let { currentFeed ->
                                    val updatedEntries = feedResult.entries + (currentFeed.entries)
                                    _feed.value = currentFeed.copy(entries = updatedEntries)
                                }
                                _feed.value?.let {
                                    it.meta.unseenCount = feedResult.meta.unseenCount
                                    it.meta.unreadCount = feedResult.meta.unreadCount
                                    it.meta.totalCount = feedResult.meta.totalCount
                                    it.pageInfo.before = feedResult.entries.firstOrNull()?.feedCursor
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(loggerTag, "Error in getUserFeedContent: ${e.message}")
            }
        }
    }

    fun archiveItem(item: FeedItem) {
        viewModelScope.launch {
            try {
                val messages = Knock.batchUpdateStatuses(messageIds = listOf(item.id), status = KnockMessageStatusBatchUpdateType.ARCHIVED)
                messages.firstOrNull()?.let { message ->
                    _feed.value?.entries = _feed.value?.entries?.filter { it.id != message.id } ?: listOf()
                    val userFeed = Knock.feedManager?.getUserFeedContent(FeedClientOptions(tenant = selectedTeam.id, hasTenant = true))
                    withContext(Dispatchers.Main) {
                        userFeed?.let { feedResult ->
                            _feed.value?.meta = feedResult.meta
                        }
                    }
                } ?: run {
                    Log.e(loggerTag, "Could not archive items at this time")
                }
            } catch (e: Exception) {
                Log.e(loggerTag, "Could not archive items at this time: ${e.message}")
            }
        }
    }

    fun updateSeenStatus() {
        viewModelScope.launch {
            if ((_feed.value?.meta?.unseenCount ?: 0) > 0) {
                try {
                    val feedOptions = FeedClientOptions(hasTenant = true, status = FeedItemScope.ALL, tenant = selectedTeam.id)
                    Knock.feedManager?.makeBulkStatusUpdate(type = BulkChannelMessageStatusUpdateType.SEEN, options = feedOptions)
                    Log.d(loggerTag, "Marked all as seen")
                } catch (e: Exception) {
                    Log.e(loggerTag, "Error in makeBulkStatusUpdate: ${e.message}")
                }
            }
        }
    }
}

