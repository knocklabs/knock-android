package app.knock.example.viewmodels
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import app.knock.client.modules.FeedManager
//import app.knock.client.Knock
//import app.knock.client.models.feed.Feed
//import app.knock.client.models.feed.FeedClientOptions
//import app.knock.client.models.feed.FeedItem
//import app.knock.client.models.feed.FeedItemArchivedScope
//import app.knock.client.models.feed.FeedItemScope
//import app.knock.client.models.messages.KnockMessageStatusUpdateType
//import app.knock.client.modules.updateMessageStatus
//import app.knock.example.Utils
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class InAppFeedViewModel : ViewModel() {
//    private val _feed = MutableStateFlow<Feed?>(null)
//    val feed: StateFlow<Feed?> = _feed.asStateFlow()
//
//    val options = FeedClientOptions(archived = FeedItemArchivedScope.EXCLUDE, triggerData = mapOf("comment" to "trigger test"))
//
//    fun initializeFeed() {
//        viewModelScope.launch {
//            try {
//                Knock.shared.feedManager = FeedManager(Utils.inAppChannelId, options)
//                val userFeed = withContext(Dispatchers.IO) {
//                    Knock.shared.feedManager?.getUserFeedContent(options)
//                }
//                userFeed?.let {
//                    _feed.value = it
//                    _feed.value?.pageInfo?.before = it.entries.firstOrNull()?.feedCursor
//                }
//
//                Knock.shared.feedManager?.connectToFeed()
//
//                Knock.shared.feedManager?.on("new-message") {
//                    viewModelScope.launch {
//                        val feedOptions = FeedClientOptions(before = feed.value?.pageInfo?.before)
//                        val result = withContext(Dispatchers.IO) {
//                            Knock.shared.feedManager?.getUserFeedContent(feedOptions)
//                        }
//                        result?.let { feedResult ->
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
//            } catch (e: Exception) {
//                Log.e(Utils.loggingTag, "Error in getUserFeedContent: ${e.message}")
//            }
//        }
//    }
//
//    fun archiveItem(item: FeedItem) {
//        viewModelScope.launch {
//            try {
//                val message = withContext(Dispatchers.IO) {
//                    Knock.shared.updateMessageStatus(item.id, KnockMessageStatusUpdateType.ARCHIVED)
//                }
//
//                val updatedEntries = _feed.value?.entries?.filterNot { it.id == message.id } ?: listOf()
//                _feed.value = _feed.value?.copy(entries = updatedEntries)
//            } catch (e: Exception) {
//                Log.e(Utils.loggingTag, "Could not archive items at this time: ${e.message}")
//            }
//        }
//    }
//
//    fun markAllAsSeen() {
//        viewModelScope.launch {
//            if ((_feed.value?.meta?.unseenCount ?: 0) > 0) {
//                try {
//                    val feedOptions = FeedClientOptions(status = FeedItemScope.ALL)
//                    withContext(Dispatchers.IO) {
//                        Knock.shared.feedManager?.makeBulkStatusUpdate(type = KnockMessageStatusUpdateType.SEEN, options = feedOptions)
//                    }
//                    _feed.value?.meta?.unseenCount = 0
//                    Log.d(Utils.loggingTag, "Marked all as seen")
//                } catch (e: Exception) {
//                    Log.e(Utils.loggingTag, "Error in makeBulkStatusUpdate: ${e.message}")
//                }
//            }
//        }
//    }
//}
//
