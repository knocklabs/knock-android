package com.example.knock_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.knock.client.FeedManager
import app.knock.client.Knock
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatus
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.batchUpdateStatuses
import app.knock.client.modules.updateMessageStatus
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

    private val loggerTag = "InAppFeedViewModel"

//    fun initializeFeed() {
//        Knock.feedManager = FeedManager(Utils.inAppChannelId, FeedClientOptions())
//
//        Knock.feedManager?.getUserFeedContent(FeedClientOptions()) { result ->
//            result.onSuccess { userFeed ->
//                _feed.value = userFeed
//                _feed.value?.pageInfo?.before = userFeed.entries.firstOrNull()?.feedCursor
//
//                Knock.feedManager?.connectToFeed()
//
//                Knock.feedManager?.on("new-message") {
//                    viewModelScope.launch {
//                        val feedOptions = FeedClientOptions(before = feed.value?.pageInfo?.before)
//                        val newMessageContentResult = withContext(Dispatchers.IO) {
//                            Knock.feedManager?.getUserFeedContent(feedOptions)
//                        }
//                        newMessageContentResult?.let { feedResult ->
//                            _feed.value?.let { currentFeed ->
//                                val updatedEntries = feedResult.entries + (currentFeed.entries)
//                                _feed.value = currentFeed.copy(entries = updatedEntries)
//                            }
//                            _feed.value?.let {
//                                it.meta.unseenCount = feedResult.meta.unseenCount
//                                it.meta.unreadCount = feedResult.meta.unreadCount
//                                it.meta.totalCount = feedResult.meta.totalCount
//                                it.pageInfo.before = feedResult.entries.firstOrNull()?.feedCursor
//                            }
//                        }
//                    }
//                }
//            }.onFailure { e ->
//                Log.e(loggerTag, "Error in getUserFeedContent: ${e.message}")
//            }
//        }
//    }

    fun initializeFeed() {
        viewModelScope.launch {
            try {
                Knock.feedManager = FeedManager(Utils.inAppChannelId, FeedClientOptions())
                val userFeed = withContext(Dispatchers.IO) {
                    Knock.feedManager?.getUserFeedContent(FeedClientOptions())
                }
                userFeed?.let {
                    _feed.value = it
                    _feed.value?.pageInfo?.before = it.entries.firstOrNull()?.feedCursor
                }

                Knock.feedManager?.connectToFeed()

                Knock.feedManager?.on("new-message") {
                    viewModelScope.launch {
                        val feedOptions = FeedClientOptions(before = feed.value?.pageInfo?.before)
                        val result = withContext(Dispatchers.IO) {
                            Knock.feedManager?.getUserFeedContent(feedOptions)
                        }
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
            } catch (e: Exception) {
                Log.e(loggerTag, "Error in getUserFeedContent: ${e.message}")
            }
        }
    }

    fun archiveItem(item: FeedItem) {
        viewModelScope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    Knock.updateMessageStatus(item.id, KnockMessageStatusUpdateType.ARCHIVED)
//                    Knock.batchUpdateStatuses(messageIds = listOf(item.id), status = KnockMessageStatusUpdateType.ARCHIVED)
                }

                val updatedEntries = _feed.value?.entries?.filterNot { it.id == message.id } ?: listOf()
                _feed.value = _feed.value?.copy(entries = updatedEntries)
//                messages.firstOrNull()?.let { message ->
//                    val updatedEntries = _feed.value?.entries?.filterNot { it.id == message.id } ?: listOf()
//                    _feed.value = _feed.value?.copy(entries = updatedEntries)
//                } ?: run {
//                    Log.e(loggerTag, "Could not archive items at this time")
//                }
            } catch (e: Exception) {
                Log.e(loggerTag, "Could not archive items at this time: ${e.message}")
            }
        }
    }

    fun markAllAsSeen() {
        viewModelScope.launch {
            if ((_feed.value?.meta?.unseenCount ?: 0) > 0) {
                try {
                    val feedOptions = FeedClientOptions(status = FeedItemScope.ALL)
                    withContext(Dispatchers.IO) {
                        Knock.feedManager?.makeBulkStatusUpdate(type = KnockMessageStatusUpdateType.SEEN, options = feedOptions)
                    }
                    _feed.value?.meta?.unseenCount = 0
                    Log.d(loggerTag, "Marked all as seen")
                } catch (e: Exception) {
                    Log.e(loggerTag, "Error in makeBulkStatusUpdate: ${e.message}")
                }
            }
        }
    }
}

