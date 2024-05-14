package app.knock.client.components
import app.knock.client.Knock
import app.knock.client.components.models.FeedNotificationRowSwipeAction
import app.knock.client.components.models.FeedTopActionButtonType
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.updateMessageStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.ZonedDateTime

// TODO: determine where to use suspend vs viewModelScope
class InAppFeedViewModel(
    var feedClientOptions: FeedClientOptions = FeedClientOptions(),
    var currentTenantId: String? = null,
    var currentFilter: InAppFeedFilter = InAppFeedFilter(FeedItemScope.ALL),
    var filterOptions: List<InAppFeedFilter> = listOf(InAppFeedFilter(FeedItemScope.ALL), InAppFeedFilter(FeedItemScope.UNREAD), InAppFeedFilter(FeedItemScope.ARCHIVED)),
    var topButtonActions: List<FeedTopActionButtonType> = listOf(FeedTopActionButtonType.MarkAllAsRead(), FeedTopActionButtonType.ArchiveRead())
) {
    val feed = MutableStateFlow(Feed()) // The current feed data.

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val brandingRequired = MutableStateFlow(true) // Controls whether to show the Knock icon on the feed interface.
    val showRefreshIndicator = MutableStateFlow(false)

    // Publisher for feed item button tap events.
    private val _didTapFeedItemButtonPublisher = MutableSharedFlow<String>()
    val didTapFeedItemButtonPublisher = _didTapFeedItemButtonPublisher.asSharedFlow()

    private val _didTapFeedItemRowPublisher = MutableSharedFlow<FeedItem>()
    val didTapFeedItemRowPublisher = _didTapFeedItemRowPublisher.asSharedFlow()

    init {
        // Set initial options based on parameters
        feedClientOptions = feedClientOptions.copy(
            status = currentFilter.scope,
            tenant = currentTenantId ?: feedClientOptions.tenant
        )
    }

    fun connectFeedAndObserveNewMessages() {
        viewModelScope.launch {
            // Placeholder for feed manager connection logic
            Knock.shared.feedManager?.connectToFeed()
            Knock.shared.feedManager?.on { message ->
                feedClientOptions.before = feed.value.pageInfo.before
                viewModelScope.launch {
                    val userFeed = Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                    userFeed?.let { mergeFeedsForNewMessageReceived(it) }
                }
            }
            refreshFeed(showIndicator = true)
            val isBrandingRequired = getBrandingRequired()
            brandingRequired.value = isBrandingRequired
        }
    }

    fun refreshFeed(showIndicator: Boolean = false) {
        viewModelScope.launch {
            if (showIndicator) {
                showRefreshIndicator.value = true
            }
            try {
                val userFeed = Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                userFeed?.let {
                    feed.value = it
                    feed.value.pageInfo.before = it.entries.firstOrNull()?.feedCursor
                }
                if (showIndicator) {
                    showRefreshIndicator.value = false
                }
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    private fun fetchNewPageOfFeedItems() {
        viewModelScope.launch {
            feed.value.pageInfo.after?.let { after ->
                feedClientOptions = feedClientOptions.copy(after = after)
                try {
                    val newPageFeed = Knock.shared.feedManager?.getUserFeedContent(feedClientOptions)
                    newPageFeed?.let { mergeFeedsForNewPageOfFeed(it) }
                } catch (e: Exception) {
                    handleFeedError(e)
                }
            }
        }
    }

    /// Determines whether there is another page of content to fetch when paginaating the feedItem list
    fun isMoreContentAvailable(): Boolean {
        return feed.value.pageInfo.after != null
    }

    // MARK: Message Update Methods
    fun archiveItem(item: FeedItem) {
        viewModelScope.launch {
            try {
                Knock.shared.updateMessageStatus(item.id, KnockMessageStatusUpdateType.ARCHIVED)
                feed.value = feed.value.copy(entries = feed.value.entries.filter { it.id != item.id })
                fetchNewMetaData()
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun archiveAll(scope: FeedItemScope) {
        viewModelScope.launch {
            try {
                Knock.shared.feedManager?.makeBulkStatusUpdate(KnockMessageStatusUpdateType.ARCHIVED, FeedClientOptions(status = scope))
                refreshFeed()
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                Knock.shared.feedManager?.makeBulkStatusUpdate(KnockMessageStatusUpdateType.READ, FeedClientOptions(status = app.knock.client.models.feed.FeedItemScope.ALL))
                val currentDate = ZonedDateTime.now()
                feed.value = feed.value.copy(entries = feed.value.entries.map { it.copy(readAt = currentDate) })
                fetchNewMetaData()
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun markAllAsSeen() {
        viewModelScope.launch {
            try {
                Knock.shared.feedManager?.makeBulkStatusUpdate(KnockMessageStatusUpdateType.SEEN, FeedClientOptions(status = app.knock.client.models.feed.FeedItemScope.ALL))
                fetchNewMetaData()
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun markAsRead(item: FeedItem) {
        viewModelScope.launch {
            try {
                Knock.shared.messageModule.updateMessageStatus(item.id, KnockMessageStatusUpdateType.READ)
                feed.value.entries.find { it.id == item.id }?.let {
                    it.readAt = ZonedDateTime.now()
                }
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun markAsUnread(item: FeedItem) {
        viewModelScope.launch {
            try {
                Knock.shared.messageModule.updateMessageStatus(item.id, KnockMessageStatusUpdateType.UNREAD) // TODO: Evaluate how we can combine some of these status type objects
                feed.value.entries.find { it.id == item.id }?.let {
                    it.readAt = null
                }
            } catch (e: Exception) {
                handleFeedError(e)
            }
        }
    }

    fun markAsInteracted(item: FeedItem) {
        viewModelScope.launch {
            try {
                Knock.shared.messageModule.updateMessageStatus(item.id, KnockMessageStatusUpdateType.INTERACTED) // TODO: Evaluate how we can combine some of these status type objects
            } catch (e: Exception) {
                handleFeedError(e)
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
            markAsInteracted(item)
        }
    }

    // MARK: Button/Swipe Interactions

    // Called when a user performs a horizontal swipe action on a row item.
    fun didSwipeRow(item: FeedItem, swipeAction: FeedNotificationRowSwipeAction) {
        viewModelScope.launch {
            when (swipeAction) {
                FeedNotificationRowSwipeAction.Archive -> archiveItem(item)
                FeedNotificationRowSwipeAction.MarkAsRead -> markAsRead(item)
                FeedNotificationRowSwipeAction.MarkAsUnread -> markAsUnread(item)
            }
        }
    }

    // Called when a user taps on one of the action buttons at the top of the list.
    fun topActionButtonTapped(action: FeedTopActionButtonType) {
        viewModelScope.launch {
            when (action) {
                is FeedTopActionButtonType.ArchiveAll -> archiveAll(scope = FeedItemScope.ALL)
                is FeedTopActionButtonType.ArchiveRead -> archiveAll(scope = FeedItemScope.READ)
                is FeedTopActionButtonType.MarkAllAsRead -> markAllAsRead()
            }
        }
    }

    // MARK: Private Methods
    private fun filterDidChange() {
        feedClientOptions.status = currentFilter.scope
        viewModelScope.launch {
            refreshFeed(showIndicator = true)
        }
    }

    private fun fetchNewMetaData() {
        viewModelScope.launch {
            try {
                val options = FeedClientOptions(tenant = currentTenantId, hasTenant = feedClientOptions.hasTenant)
                val feed = withContext(Dispatchers.IO) {
                    Knock.shared.feedManager?.getUserFeedContent(options)
                }
                feed?.let {
                    withContext(Dispatchers.Main) {
                        feed.meta = it.meta
                    }
                }
            } catch (error: Exception) {
                handleFeedError(error)
            }
        }
    }

    private fun mergeFeedsForNewMessageReceived(newFeed: Feed) {
        feed.value = feed.value.copy(
            entries = newFeed.entries + feed.value.entries, // TODO: Test this to ensure new values are being inserted correctly
            meta = newFeed.meta,
            pageInfo = feed.value.pageInfo.copy(before = newFeed.entries.firstOrNull()?.feedCursor)
        )
    }

    private fun mergeFeedsForNewPageOfFeed(newFeed: Feed) {
        feed.value = feed.value.copy(
            entries = feed.value.entries + newFeed.entries, // TODO: Test this to ensure new values are being inserted correctly
            meta = newFeed.meta,
            pageInfo = feed.value.pageInfo.copy(after = newFeed.pageInfo.after)
        )
    }

    private fun getBrandingRequired(): Boolean {
        return true
//        return feedManager.getFeedSettings()?.brandingRequired ?: false
    }

    private fun handleFeedError(error: Exception) {
        // Log the error or handle it appropriately
    }
}
