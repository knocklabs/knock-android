package app.knock.client.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.components.models.FeedItemButtonEvent
import app.knock.client.components.models.FeedTopActionButtonType
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.components.themes.FeedNotificationRowSwipeAction
import app.knock.client.logError
import app.knock.client.models.feed.BlockActionButton
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

@Suppress("unused")
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

    private val _feed = MutableStateFlow(Feed()) // The current feed data.
    val feed: StateFlow<Feed> = _feed.asStateFlow()

    private val _brandingRequired = MutableStateFlow(true) // Controls whether to show the Knock icon on the feed interface.
    val brandingRequired: StateFlow<Boolean> = _brandingRequired.asStateFlow()

    private val _showRefreshIndicator = MutableStateFlow(false)
    val showRefreshIndicator: StateFlow<Boolean> = _showRefreshIndicator.asStateFlow()

    private val _isRefreshingFromPull = MutableStateFlow(false)
    val isRefreshingFromPull: StateFlow<Boolean> = _isRefreshingFromPull.asStateFlow()

    // Publisher for feed item button tap events.
    private val _didTapFeedItemButtonPublisher = MutableSharedFlow<FeedItemButtonEvent>()
    val didTapFeedItemButtonPublisher = _didTapFeedItemButtonPublisher.asSharedFlow()

    private val _didTapFeedItemRowPublisher = MutableSharedFlow<FeedItem>()
    val didTapFeedItemRowPublisher = _didTapFeedItemRowPublisher.asSharedFlow()

    private val shouldHideArchived: Boolean
        get() = feedClientOptions.archived == FeedItemArchivedScope.EXCLUDE || feedClientOptions.archived == null

    init {
        // Set initial options based on parameters
        feedClientOptions = feedClientOptions.copy(
            status = currentFilter.value.scope
        )
    }

    fun setFilterOptions(options: List<InAppFeedFilter>) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _filterOptions.value = options
            }
        }
    }

    fun setCurrentFilter(filter: InAppFeedFilter) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _currentFilter.value = filter
            }
            feedClientOptions.status = currentFilter.value.scope
            refreshFeed(showIndicator = true)
        }
    }

    fun connectFeedAndObserveNewMessages() {
        viewModelScope.launch {
            // Placeholder for feed manager connection logic
            Knock.shared.feedManager?.connectToFeed()
            Knock.shared.feedManager?.on { _ ->
                feedClientOptions.before = _feed.value.pageInfo.before
                viewModelScope.launch {
                    val userFeed = withContext(Dispatchers.IO) {
                        Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                    }
                    userFeed?.let { mergeFeedsForNewMessageReceived(it) }
                }
            }
            val isBrandingRequired = getBrandingRequired()
            withContext(Dispatchers.Main) {
                _brandingRequired.value = isBrandingRequired
            }
            refreshFeed(showIndicator = true)
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _isRefreshingFromPull.value = true
            }

            refreshFeed()

            withContext(Dispatchers.Main) {
                _isRefreshingFromPull.value = false
            }
        }
    }

    suspend fun refreshFeed(showIndicator: Boolean = false) {
        if (showIndicator) _showRefreshIndicator.emit(true)
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
            withContext(Dispatchers.Main) {
                _feed.value = it
                _showRefreshIndicator.value = false
            }
            _feed.value.pageInfo.before = _feed.value.entries.firstOrNull()?.feedCursor
            feedClientOptions.status = originalStatus
        }
    }

    fun fetchNewPageOfFeedItems() {
        viewModelScope.launch {
            val after = _feed.value.pageInfo.after ?: return@launch
            feedClientOptions.after = after

            val newFeed = withContext(Dispatchers.IO) {
                try {
                    Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                } catch (e: Exception) {
                    null
                }
            }

            newFeed?.let {
                mergeFeedsForNewPageOfFeed(it)
            }
        }
    }

    /// Determines whether there is another page of content to fetch when paginating the feedItem list
    fun isMoreContentAvailable(): Boolean {
        return _feed.value.pageInfo.after != null
    }

    // MARK: Message Engagement Status Updates

    suspend fun bulkUpdateMessageEngagementStatus(
        updatedStatus: KnockMessageStatusUpdateType,
        archivedScope: FeedItemScope = FeedItemScope.ALL
    ) {
        when (updatedStatus) {
            KnockMessageStatusUpdateType.SEEN -> if (_feed.value.meta.unseenCount <= 0) return
            KnockMessageStatusUpdateType.READ -> if (_feed.value.meta.unreadCount <= 0) return
            else -> {}
        }

        val feedOptionsForUpdate = feedClientOptions.copy(status = archivedScope)

        try {
            optimisticallyBulkUpdateStatus(updatedStatus, archivedScope)
            withContext(Dispatchers.IO) {
                Knock.shared.feedManager?.makeBulkStatusUpdate(updatedStatus, feedOptionsForUpdate)
            }
        } catch (e: Exception) {
            logError("Failed: bulkUpdateMessageStatus for status: $updatedStatus", e)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun updateMessageEngagementStatus(item: FeedItem, updatedStatus: KnockMessageStatusUpdateType) {
        when (updatedStatus) {
            KnockMessageStatusUpdateType.SEEN -> if (item.seenAt != null) return
            KnockMessageStatusUpdateType.READ -> if (item.readAt != null) return
            KnockMessageStatusUpdateType.INTERACTED -> if (item.insertedAt != null) return
            KnockMessageStatusUpdateType.ARCHIVED -> if (item.archivedAt != null) return
            KnockMessageStatusUpdateType.UNREAD -> if (item.readAt == null) return
            KnockMessageStatusUpdateType.UNSEEN -> if (item.seenAt == null) return
            KnockMessageStatusUpdateType.UNARCHIVED -> if (item.archivedAt == null) return
        }
        try {
            optimisticallyUpdateStatusForItem(item, updatedStatus)
            withContext(Dispatchers.IO) {
                Knock.shared.messageModule.updateMessageStatus(item.id, updatedStatus)
            }
        } catch (e: Exception) {
            logError("Failed: updateMessageStatus for status: $updatedStatus", e)
        }
    }

    // MARK: FeedItemRow Interactions

    fun feedItemButtonTapped(item: FeedItem, actionButton: BlockActionButton) {
        viewModelScope.launch {
            _didTapFeedItemButtonPublisher.emit(FeedItemButtonEvent(item, actionButton))
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
    fun didSwipeRow(item: FeedItem, swipeAction: FeedNotificationRowSwipeAction, useInverse: Boolean) {
        viewModelScope.launch {
            when (swipeAction) {
                FeedNotificationRowSwipeAction.ARCHIVE -> updateMessageEngagementStatus(item, if (useInverse) KnockMessageStatusUpdateType.UNARCHIVED else KnockMessageStatusUpdateType.ARCHIVED)
                FeedNotificationRowSwipeAction.MARK_AS_READ -> updateMessageEngagementStatus(item, if (useInverse) KnockMessageStatusUpdateType.UNREAD else KnockMessageStatusUpdateType.READ)
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
        val updatedEntries = updateEntriesStatusForOptimisticBulkUpdate(feed.value.entries, updatedStatus, date, archivedScope)

        // Filter entries based on the currentFilter
        val filteredEntries = if (currentFilter.value.scope != FeedItemScope.ALL || updatedStatus == KnockMessageStatusUpdateType.ARCHIVED) {
            filterEntries(updatedEntries, currentFilter.value.scope)
        } else {
            updatedEntries
        }

        withContext(Dispatchers.Main) {
            _feed.emit(feed.value.copy(entries = filteredEntries))
            optimisticallyUpdateMetaCountsAfterBulkUpdate(updatedStatus)
        }
    }

    private fun updateEntriesStatusForOptimisticBulkUpdate(
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

    private suspend fun optimisticallyUpdateMetaCountsAfterBulkUpdate(status: KnockMessageStatusUpdateType) {
        when (status) {
            KnockMessageStatusUpdateType.SEEN -> _feed.emit(_feed.value.copy(meta = _feed.value.meta.copy(unseenCount = 0)))
            KnockMessageStatusUpdateType.READ -> _feed.emit(_feed.value.copy(meta = _feed.value.meta.copy(unreadCount = 0)))
            KnockMessageStatusUpdateType.UNREAD -> _feed.emit(_feed.value.copy(meta = _feed.value.meta.copy(unreadCount = _feed.value.entries.size)))
            KnockMessageStatusUpdateType.UNSEEN -> _feed.emit(_feed.value.copy(meta = _feed.value.meta.copy(unseenCount = _feed.value.entries.size)))
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
        val index = _feed.value.entries.indexOfFirst { it.id == item.id }
        if (index != -1) {
            val mutableEntries = _feed.value.entries.toMutableList()
            val updatedItem = when (status) {
                KnockMessageStatusUpdateType.READ -> {
                    if (item.readAt == null) {
                        item.copy(
                            readAt = ZonedDateTime.now()
                        ).also {
                            if (_feed.value.meta.unreadCount > 0) {
                                _feed.value.meta = _feed.value.meta.copy(unreadCount = _feed.value.meta.unreadCount - 1)
                            }
                        }
                    } else item
                }
                KnockMessageStatusUpdateType.UNREAD -> {
                    item.copy(readAt = null).also {
                        _feed.value.meta = _feed.value.meta.copy(unreadCount = _feed.value.meta.unreadCount + 1)
                    }
                }
                KnockMessageStatusUpdateType.SEEN -> {
                    if (item.seenAt == null) {
                        item.copy(
                            seenAt = ZonedDateTime.now()
                        ).also {
                            if (_feed.value.meta.unseenCount > 0) {
                                _feed.value.meta = _feed.value.meta.copy(unseenCount = _feed.value.meta.unseenCount - 1)
                            }
                        }
                    } else item
                }
                KnockMessageStatusUpdateType.UNSEEN -> {
                    item.copy(seenAt = null).also {
                        _feed.value.meta = _feed.value.meta.copy(unseenCount = _feed.value.meta.unseenCount + 1)
                    }
                }
                KnockMessageStatusUpdateType.INTERACTED -> {
                    if (item.readAt == null) {
                        item.copy(
                            readAt = ZonedDateTime.now(),
                            interactedAt = ZonedDateTime.now()
                        ).also {
                            if (_feed.value.meta.unreadCount > 0) {
                                _feed.value.meta = _feed.value.meta.copy(unreadCount = _feed.value.meta.unreadCount - 1)
                            }
                        }
                    } else {
                        item.copy(interactedAt = ZonedDateTime.now())
                    }
                }
                KnockMessageStatusUpdateType.ARCHIVED -> {
                    item.copy(archivedAt = ZonedDateTime.now())
                }
                else -> item
            }

            mutableEntries[index] = updatedItem

            // Logic to remove the entry if it matches specific statuses
            when (status) {
                KnockMessageStatusUpdateType.READ -> {
                    if (feedClientOptions.status == FeedItemScope.UNREAD) {
                        mutableEntries.removeAt(index)
                    }
                }
                KnockMessageStatusUpdateType.UNREAD -> {
                    if (feedClientOptions.status == FeedItemScope.READ) {
                        mutableEntries.removeAt(index)
                    }
                }
                KnockMessageStatusUpdateType.SEEN -> {
                    if (feedClientOptions.status == FeedItemScope.UNSEEN) {
                        mutableEntries.removeAt(index)
                    }
                }
                KnockMessageStatusUpdateType.UNSEEN -> {
                    if (feedClientOptions.status == FeedItemScope.SEEN) {
                        mutableEntries.removeAt(index)
                    }
                }
                KnockMessageStatusUpdateType.INTERACTED -> {
                    if (feedClientOptions.status == FeedItemScope.READ) {
                        mutableEntries.removeAt(index)
                    }
                }
                KnockMessageStatusUpdateType.ARCHIVED -> {
                    if (shouldHideArchived) {
                        mutableEntries.removeAt(index)
                    }
                }
                else -> {}
            }

            withContext(Dispatchers.Main) {
                _feed.emit(_feed.value.copy(entries = mutableEntries))
            }
        }
    }

    private suspend fun fetchNewMetaData() {
        try {
            val newFeed = withContext(Dispatchers.IO) {
                Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
            }
            newFeed?.let {
                withContext(Dispatchers.Main) {
                    _feed.emit(_feed.value.copy(meta = it.meta))
                }
            }
        } catch (error: Exception) {
            logError("fetchNewMetaData Failed", error)
        }
    }

    private suspend fun mergeFeedsForNewMessageReceived(newFeed: Feed) {
        withContext(Dispatchers.Main) {
            _feed.emit(
                _feed.value.copy(
                    entries = newFeed.entries + _feed.value.entries,
                    meta = newFeed.meta,
                    pageInfo = _feed.value.pageInfo.copy(before = newFeed.entries.firstOrNull()?.feedCursor)
                )
            )
        }
    }

    private suspend fun mergeFeedsForNewPageOfFeed(newFeed: Feed) {
        withContext(Dispatchers.Main) {
            _feed.emit(
                _feed.value.copy(
                    entries = _feed.value.entries + newFeed.entries,
                    meta = newFeed.meta,
                    pageInfo = _feed.value.pageInfo.copy(after = newFeed.pageInfo.after)
                )
            )
        }
    }

    private suspend fun getBrandingRequired(): Boolean {
        return withContext(Dispatchers.IO) {
            Knock.shared.feedManager?.getFeedSettings()?.features?.brandingRequired ?: true
        }
    }

    private fun logError(message: String, error: Exception?) {
        Knock.shared.logError(KnockLogCategory.FEED, message, exception = error)
    }
}


class InAppFeedViewModelFactory(
    private val context: Context,
    private val feedClientOptions: FeedClientOptions = FeedClientOptions(),
    private val currentFilter: InAppFeedFilter? = null,
    private var filterOptions: List<InAppFeedFilter> = listOf(InAppFeedFilter(context, FeedItemScope.ALL), InAppFeedFilter(context, FeedItemScope.UNREAD), InAppFeedFilter(context, FeedItemScope.ARCHIVED)),
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