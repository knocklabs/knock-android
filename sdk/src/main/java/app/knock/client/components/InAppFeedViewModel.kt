package app.knock.client.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.components.models.FeedNotificationRowSwipeAction
import app.knock.client.components.models.FeedTopActionButtonType
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.logError
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemArchivedScope
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZonedDateTime

// TODO: determine where to use suspend vs viewModelScope
class InAppFeedViewModel(
    var feedClientOptions: FeedClientOptions,
    initialCurrentFilter: InAppFeedFilter,
    initialFilterOptions: List<InAppFeedFilter>,
    var topButtonActions: List<FeedTopActionButtonType>
): ViewModel() {
    private val _filterOptions = MutableStateFlow(initialFilterOptions)
    val filterOptions: StateFlow<List<InAppFeedFilter>> = _filterOptions.asStateFlow()

    private val _currentFilter = MutableStateFlow(initialCurrentFilter)
    val currentFilter: StateFlow<InAppFeedFilter> = _currentFilter.asStateFlow()

    var feed = MutableStateFlow(Feed()) // The current feed data.
    val brandingRequired = MutableStateFlow(true) // Controls whether to show the Knock icon on the feed interface.
    val showRefreshIndicator = MutableStateFlow(false)

    private val _isRefreshingFromPull = MutableStateFlow(false)
    val isRefreshingFromPull: StateFlow<Boolean>
        get() = _isRefreshingFromPull.asStateFlow()

    // Publisher for feed item button tap events.
    private val _didTapFeedItemButtonPublisher = MutableSharedFlow<String>()
    val didTapFeedItemButtonPublisher = _didTapFeedItemButtonPublisher.asSharedFlow()

    private val _didTapFeedItemRowPublisher = MutableSharedFlow<FeedItem>()
    val didTapFeedItemRowPublisher = _didTapFeedItemRowPublisher.asSharedFlow()

    private val shouldHideArchived: Boolean
        get() = feedClientOptions.archived == FeedItemArchivedScope.EXCLUDE || feedClientOptions.archived == null

    init {
        // Set initial options based on parameters
        feedClientOptions = feedClientOptions.copy(
            status = currentFilter.value.scope,
            tenant = feedClientOptions.tenant
        )
    }

    fun setFilterOptions(options: List<InAppFeedFilter>) {
        _filterOptions.value = options
    }

    fun setCurrentFilter(filter: InAppFeedFilter) {
        _currentFilter.value = filter
        viewModelScope.launch {
            feedClientOptions.status = currentFilter.value.scope
            refreshFeed(showIndicator = true)
        }
    }

    fun connectFeedAndObserveNewMessages() {
        viewModelScope.launch {
            // Placeholder for feed manager connection logic
            Knock.shared.feedManager?.connectToFeed()
            Knock.shared.feedManager?.on { _ ->
                feedClientOptions.before = feed.value.pageInfo.before
                viewModelScope.launch {
                    val userFeed = Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                    userFeed?.let { mergeFeedsForNewMessageReceived(it) }
                }
            }
            val isBrandingRequired = getBrandingRequired()
            brandingRequired.value = isBrandingRequired
            refreshFeed(showIndicator = true)
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            _isRefreshingFromPull.value = true // No need to switch to Dispatchers.Main for this
            refreshFeed()
            _isRefreshingFromPull.value = false
        }
    }

    suspend fun refreshFeed(showIndicator: Boolean = false) {
        if (showIndicator) showRefreshIndicator.value = true
        val originalStatus = feedClientOptions.status
        val archived = if (feedClientOptions.status == FeedItemScope.ARCHIVED) FeedItemArchivedScope.ONLY else null
        val status = if (feedClientOptions.status == FeedItemScope.ARCHIVED) FeedItemScope.ALL else feedClientOptions.status
        feedClientOptions.archived = archived
        feedClientOptions.status = status
        feedClientOptions.before = null
        feedClientOptions.after = null

        val userFeed = withContext(Dispatchers.IO) {
            try {
                Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
            } catch (e: Exception) {
                null
            }
        }

        userFeed?.let {
            feed.value = it
            feed.value.pageInfo.before = feed.value.entries.firstOrNull()?.feedCursor
            feedClientOptions.status = originalStatus
            showRefreshIndicator.value = false
        }
    }

    fun fetchNewPageOfFeedItems() {
        viewModelScope.launch {
            val after = feed.value.pageInfo.after ?: return@launch
            feedClientOptions.after = after

            withContext(Dispatchers.IO) {
                try {
                    Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)?.let {
                        mergeFeedsForNewPageOfFeed(it)
                    }
                } catch (e: Exception) {
                    logError("fetchNewPageOfFeedItems Failed:", e)
                }
            }
        }
    }

    /// Determines whether there is another page of content to fetch when paginaating the feedItem list
    fun isMoreContentAvailable(): Boolean {
        return feed.value.pageInfo.after != null
    }

    // MARK: Message Engagement Status Updates

    fun bulkUpdateMessageEngagementStatus(
        updatedStatus: KnockMessageStatusUpdateType,
        archivedScope: FeedItemScope = FeedItemScope.ALL
    ) {
        viewModelScope.launch {
            when (updatedStatus) {
                KnockMessageStatusUpdateType.SEEN -> if (feed.value.meta.unseenCount <= 0) return@launch
                KnockMessageStatusUpdateType.READ -> if (feed.value.meta.unreadCount <= 0) return@launch
                else -> {}
            }

            val feedOptionsForUpdate = feedClientOptions.copy(status = archivedScope)

            try {
                Knock.shared.feedManager?.makeBulkStatusUpdate(updatedStatus, feedOptionsForUpdate)?.let {
                    optimisticallyBulkUpdateStatus(updatedStatus, archivedScope)
                }
            } catch (e: Exception) {
                logError("Failed: bulkUpdateMessageStatus for status: $updatedStatus", e)
            }
        }
    }

    fun updateMessageEngagementStatus(item: FeedItem, updatedStatus: KnockMessageStatusUpdateType) {
        viewModelScope.launch {
            when (updatedStatus) {
                KnockMessageStatusUpdateType.SEEN -> if (item.seenAt != null) return@launch
                KnockMessageStatusUpdateType.READ -> if (item.readAt != null) return@launch
                KnockMessageStatusUpdateType.INTERACTED -> if (item.insertedAt != null) return@launch
                KnockMessageStatusUpdateType.ARCHIVED -> if (item.archivedAt != null) return@launch
                KnockMessageStatusUpdateType.UNREAD -> if (item.readAt == null) return@launch
                KnockMessageStatusUpdateType.UNSEEN -> if (item.seenAt == null) return@launch
                KnockMessageStatusUpdateType.UNARCHIVED -> if (item.archivedAt == null) return@launch
            }
            try {
                Knock.shared.messageModule.updateMessageStatus(item.id, updatedStatus).let {
                    optimisticallyUpdateStatusForItem(item, updatedStatus)
                    fetchNewMetaData()
                }
            } catch (e: Exception) {
                logError("Failed: updateMessageStatus for status: $updatedStatus", e)
            }
        }
    }

    // MARK: FeedItemRow Interactions

    fun feedItemButtonTapped(item: FeedItem, actionString: String) {
        viewModelScope.launch {
            _didTapFeedItemButtonPublisher.emit(actionString)
        }
    }

    fun feedItemRowTapped(item: FeedItem) {
        viewModelScope.launch {
            _didTapFeedItemRowPublisher.emit(item)
            updateMessageEngagementStatus(item, KnockMessageStatusUpdateType.INTERACTED)
        }
    }

    // MARK: Button/Swipe Interactions

    // Called when a user performs a horizontal swipe action on a row item.
    fun didSwipeRow(item: FeedItem, swipeAction: FeedNotificationRowSwipeAction) {
        viewModelScope.launch {
            when (swipeAction) {
                FeedNotificationRowSwipeAction.Archive -> updateMessageEngagementStatus(item, KnockMessageStatusUpdateType.ARCHIVED)
                FeedNotificationRowSwipeAction.MarkAsRead -> updateMessageEngagementStatus(item, KnockMessageStatusUpdateType.READ)
                FeedNotificationRowSwipeAction.MarkAsUnread -> updateMessageEngagementStatus(item, KnockMessageStatusUpdateType.UNREAD)
            }
        }
    }

    // Called when a user taps on one of the action buttons at the top of the list.
    fun topActionButtonTapped(action: FeedTopActionButtonType) {
        viewModelScope.launch {
            when (action) {
                is FeedTopActionButtonType.ArchiveAll -> bulkUpdateMessageEngagementStatus(KnockMessageStatusUpdateType.ARCHIVED)
                is FeedTopActionButtonType.ArchiveRead -> bulkUpdateMessageEngagementStatus(KnockMessageStatusUpdateType.ARCHIVED, FeedItemScope.READ)
                is FeedTopActionButtonType.MarkAllAsRead -> bulkUpdateMessageEngagementStatus(KnockMessageStatusUpdateType.READ)
            }
        }
    }

    // MARK: Private Methods

    internal suspend fun optimisticallyBulkUpdateStatus(
        updatedStatus: KnockMessageStatusUpdateType,
        archivedScope: FeedItemScope = FeedItemScope.ALL
    ) {
        val date = ZonedDateTime.now()
        val updatedEntries = updateEntriesStatus(feed.value.entries, updatedStatus, date, archivedScope)

        // Filter entries based on the currentFilter
        val filteredEntries = if (currentFilter.value.scope != FeedItemScope.ALL || updatedStatus == KnockMessageStatusUpdateType.ARCHIVED) {
            filterEntries(updatedEntries, currentFilter.value.scope)
        } else {
            updatedEntries
        }

        withContext(Dispatchers.Main) {
            feed.value = feed.value.copy(entries = filteredEntries)
            optimisticallyUpdateMetaCounts(updatedStatus)
        }
    }

    private fun updateEntriesStatus(
        entries: List<FeedItem>,
        status: KnockMessageStatusUpdateType,
        date: ZonedDateTime,
        archivedScope: FeedItemScope
    ): List<FeedItem> {
        return entries.mapNotNull { item ->
            var shouldHide = false
            when (status) {
                KnockMessageStatusUpdateType.SEEN -> if (item.seenAt == null) item.seenAt = date
                KnockMessageStatusUpdateType.READ -> if (item.readAt == null) item.readAt = date
                KnockMessageStatusUpdateType.INTERACTED -> if (item.interactedAt == null) item.interactedAt = date
                KnockMessageStatusUpdateType.ARCHIVED -> {
                    if (item.archivedAt == null && shouldArchive(item, archivedScope)) {
                        item.archivedAt = date
                        shouldHide = shouldHideArchived
                    }
                }
                KnockMessageStatusUpdateType.UNREAD -> item.readAt = null
                KnockMessageStatusUpdateType.UNSEEN -> item.seenAt = null
                else -> {}
            }
            if (shouldHide) {
                null
            } else {
                item
            }
        }
    }

    private fun shouldArchive(item: FeedItem, scope: FeedItemScope): Boolean {
        return when (scope) {
            FeedItemScope.INTERACTED -> item.interactedAt != null
            FeedItemScope.UNREAD -> item.readAt == null
            FeedItemScope.READ -> item.readAt != null
            FeedItemScope.UNSEEN -> item.seenAt == null
            FeedItemScope.SEEN -> item.seenAt != null
            else -> true
        }
    }

    private fun optimisticallyUpdateMetaCounts(status: KnockMessageStatusUpdateType) {
        when (status) {
            KnockMessageStatusUpdateType.SEEN -> feed.value.meta.unseenCount = 0
            KnockMessageStatusUpdateType.READ -> feed.value.meta.unreadCount = 0
            KnockMessageStatusUpdateType.UNREAD -> feed.value.meta.unreadCount = feed.value.entries.size
            KnockMessageStatusUpdateType.UNSEEN -> feed.value.meta.unseenCount = feed.value.entries.size
            else -> {}
        }
    }

    private fun filterEntries(entries: List<FeedItem>, scope: FeedItemScope): List<FeedItem> {
        return entries.filter {
            when (scope) {
                FeedItemScope.UNREAD -> it.readAt == null
                FeedItemScope.READ -> it.readAt != null
                FeedItemScope.UNSEEN -> it.seenAt == null
                FeedItemScope.SEEN -> it.seenAt != null
                FeedItemScope.ARCHIVED -> it.archivedAt != null
                else -> true
            }
        }
    }

    internal suspend fun optimisticallyUpdateStatusForItem(item: FeedItem, status: KnockMessageStatusUpdateType) {
        val index = feed.value.entries.indexOfFirst { it.id == item.id }
        if (index != -1) {
            withContext(Dispatchers.Main) {
                val mutableEntries = feed.value.entries.toMutableList()
                when (status) {
                    KnockMessageStatusUpdateType.READ -> {
                        mutableEntries[index].readAt = ZonedDateTime.now()
                        if (feed.value.meta.unreadCount > 0) {
                            feed.value.meta.unreadCount -= 1
                        }
                        if (feedClientOptions.status == FeedItemScope.UNREAD) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    KnockMessageStatusUpdateType.UNREAD -> {
                        mutableEntries[index].readAt = null
                        feed.value.meta.unreadCount += 1
                        if (feedClientOptions.status == FeedItemScope.READ) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    KnockMessageStatusUpdateType.SEEN -> {
                        mutableEntries[index].seenAt = ZonedDateTime.now()
                        if (feed.value.meta.unseenCount > 0) {
                            feed.value.meta.unseenCount -= 1
                        }
                        if (feedClientOptions.status == FeedItemScope.UNSEEN) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    KnockMessageStatusUpdateType.UNSEEN -> {
                        mutableEntries[index].seenAt = null
                        feed.value.meta.unseenCount += 1
                        if (feedClientOptions.status == FeedItemScope.SEEN) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    KnockMessageStatusUpdateType.INTERACTED -> {
                        if (item.readAt == null) {
                            mutableEntries[index].readAt = ZonedDateTime.now()
                            if (feed.value.meta.unreadCount > 0) {
                                feed.value.meta.unreadCount -= 1
                            }
                        }
                        mutableEntries[index].interactedAt = ZonedDateTime.now()
                        if (feedClientOptions.status == FeedItemScope.READ) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    KnockMessageStatusUpdateType.ARCHIVED -> {
                        mutableEntries[index].archivedAt = ZonedDateTime.now()
                        if (shouldHideArchived) {
                            mutableEntries.removeAt(index)
                        }
                    }
                    else -> {}
                }
                feed.value.entries = mutableEntries
            }
        }
    }

    private suspend fun fetchNewMetaData() {
        try {
            val feed = withContext(Dispatchers.IO) {
                Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
            }
            feed?.let {
                withContext(Dispatchers.Main) {
                    feed.meta = it.meta
                }
            }
        } catch (error: Exception) {
            logError("fetchNewMetaData Failed", error)
        }
    }

    private suspend fun mergeFeedsForNewMessageReceived(newFeed: Feed) {
        withContext(Dispatchers.Main) {
            feed.value = feed.value.copy(
                entries = newFeed.entries + feed.value.entries,
                meta = newFeed.meta,
                pageInfo = feed.value.pageInfo.copy(before = newFeed.entries.firstOrNull()?.feedCursor)
            )
        }
    }

    private suspend fun mergeFeedsForNewPageOfFeed(newFeed: Feed) {
        withContext(Dispatchers.Main) {
            feed.value = feed.value.copy(
                entries = feed.value.entries + newFeed.entries, // TODO: Test this to ensure new values are being inserted correctly
                meta = newFeed.meta,
                pageInfo = feed.value.pageInfo.copy(after = newFeed.pageInfo.after)
            )
        }
    }

    private fun getBrandingRequired(): Boolean {
        return true
//        return Knock.shared.feedManager.getFeedSettings()?.brandingRequired ?: false
    }

    private fun logError(message: String, error: Exception?) {
        Knock.shared.logError(KnockLogCategory.FEED, message, exception = error)
    }
}


class InAppFeedViewModelFactory(
    private val feedClientOptions: FeedClientOptions = FeedClientOptions(),
    private val currentFilter: InAppFeedFilter? = null,
    private var filterOptions: List<InAppFeedFilter> = listOf(InAppFeedFilter(FeedItemScope.ALL), InAppFeedFilter(FeedItemScope.UNREAD), InAppFeedFilter(FeedItemScope.ARCHIVED)),
    private var topButtonActions: List<FeedTopActionButtonType> = listOf(FeedTopActionButtonType.MarkAllAsRead(), FeedTopActionButtonType.ArchiveRead())
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InAppFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InAppFeedViewModel(
                feedClientOptions,
                currentFilter ?: filterOptions.first(),
                filterOptions,
                topButtonActions
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}