package app.knock.client

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.models.FeedTopActionButtonType
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemArchivedScope
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class InAppFeedViewModelTests {

    private lateinit var viewModel: InAppFeedViewModel
    private lateinit var viewModelStore: ViewModelStore

    private val mainDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        // Get application context for initializing the ViewModelFactory
        viewModelStore = ViewModelStore()

        // Create an instance of the ViewModelFactory
        val factory = InAppFeedViewModelFactory(
            feedClientOptions = FeedClientOptions(),
            currentFilter = InAppFeedFilter(FeedItemScope.ALL),
            filterOptions = listOf(
                InAppFeedFilter(FeedItemScope.ALL),
                InAppFeedFilter(FeedItemScope.UNREAD),
                InAppFeedFilter(FeedItemScope.ARCHIVED)
            ),
            topButtonActions = listOf(
                FeedTopActionButtonType.MarkAllAsRead(),
                FeedTopActionButtonType.ArchiveRead()
            )
        )

        // Use the factory to create an instance of the ViewModel
        viewModel = ViewModelProvider(
            viewModelStore,
            factory
        )[InAppFeedViewModel::class.java]
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        // Reset the Main dispatcher
        Dispatchers.resetMain()
    }

    private fun generateTestFeedItem(status: KnockMessageStatusUpdateType): FeedItem {
        val item = FeedItem(
            id = "",
            seenAt = null,
            readAt = null,
            interactedAt = null,
            archivedAt = null,
            insertedAt = null,
            actors = listOf(),
            clickedAt = null,
            feedCursor = "",
            blocks = listOf(),
            totalActors = 0,
            totalActivities = 0
        )
        return when (status) {
            KnockMessageStatusUpdateType.ARCHIVED -> item.copy(archivedAt = ZonedDateTime.now())
            KnockMessageStatusUpdateType.UNARCHIVED -> item.copy(archivedAt = null)
            KnockMessageStatusUpdateType.INTERACTED -> item.copy(interactedAt = ZonedDateTime.now(), readAt = ZonedDateTime.now())
            KnockMessageStatusUpdateType.UNREAD -> item.copy(readAt = null)
            KnockMessageStatusUpdateType.READ -> item.copy(readAt = ZonedDateTime.now())
            KnockMessageStatusUpdateType.UNSEEN -> item.copy(seenAt = null)
            KnockMessageStatusUpdateType.SEEN -> item.copy(seenAt = ZonedDateTime.now())
            else -> item
        }
    }

    @Test
    fun testOptimisticMarkItemAsRead() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        viewModel.feed.value.entries = listOf(item)
        viewModel.feed.value.meta.unreadCount = 1
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.READ)
        assertTrue(viewModel.feed.value.entries.first().readAt != null)
        assertTrue(viewModel.feed.value.meta.unreadCount == 0)
    }

    @Test
    fun testOptimisticMarkItemAsReadWithUnreadFilter() = runTest {
        viewModel.feedClientOptions.status = FeedItemScope.UNREAD
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.READ)
        viewModel.feed.value.entries = listOf(item)
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.READ)
        assertTrue(viewModel.feed.value.entries.isEmpty())
    }

    @Test
    fun testOptimisticMarkItemAsSeen() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNSEEN)
        viewModel.feed.value.entries = listOf(item)
        viewModel.feed.value.meta.unseenCount = 1
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.SEEN)
        assertTrue(viewModel.feed.value.entries.first().seenAt != null)
        assertTrue(viewModel.feed.value.meta.unseenCount == 0)
    }

    @Test
    fun testOptimisticMarkItemAsSeenWithUnseenFilter() = runTest {
        viewModel.feedClientOptions.status = FeedItemScope.UNSEEN
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.SEEN)
        viewModel.feed.value.entries = listOf(item)
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.SEEN)
        assertTrue(viewModel.feed.value.entries.isEmpty())
    }

    @Test
    fun testOptimisticMarkItemAsArchived() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.ARCHIVED)
        viewModel.feed.value.entries = listOf(item)
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.SEEN)
        assertTrue(viewModel.feed.value.entries.first().archivedAt != null)
    }

    @Test
    fun testOptimisticMarkItemAsArchivedWithNoArchivedFilter() = runTest {
        viewModel.feedClientOptions.status = FeedItemScope.ALL
        viewModel.feedClientOptions.archived = FeedItemArchivedScope.EXCLUDE
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNARCHIVED)
        viewModel.feed.value.entries = listOf(item)
        viewModel.optimisticallyUpdateStatusForItem(item, KnockMessageStatusUpdateType.ARCHIVED)
        assertTrue(viewModel.feed.value.entries.isEmpty())
    }

    @Test
    fun testOptimisticBulkMarkItemsAsRead() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item2 = generateTestFeedItem(KnockMessageStatusUpdateType.SEEN)
        val item3 = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item4 = generateTestFeedItem(KnockMessageStatusUpdateType.READ)

        viewModel.feed.value.entries = listOf(item, item2, item3, item4)
        viewModel.feed.value.meta.unreadCount = 3
        viewModel.optimisticallyBulkUpdateStatus(KnockMessageStatusUpdateType.READ)
        assertTrue(viewModel.feed.value.entries.first().readAt != null)
        assertTrue(viewModel.feed.value.meta.unreadCount == 0)
    }

    @Test
    fun testOptimisticBulkMarkItemAsArchived() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item2 = generateTestFeedItem(KnockMessageStatusUpdateType.SEEN)
        val item3 = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item4 = generateTestFeedItem(KnockMessageStatusUpdateType.READ)

        viewModel.feed.value.entries = listOf(item, item2, item3, item4)
        viewModel.optimisticallyBulkUpdateStatus(KnockMessageStatusUpdateType.ARCHIVED)
        assertTrue(viewModel.feed.value.meta.unreadCount == 0)
        assertTrue(viewModel.feed.value.entries.isEmpty())
    }

    @Test
    fun testOptimisticBulkMarkItemAsArchivedAndShouldHideArchived() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item2 = generateTestFeedItem(KnockMessageStatusUpdateType.SEEN)
        val item3 = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item4 = generateTestFeedItem(KnockMessageStatusUpdateType.READ)

        viewModel.feed.value.entries = listOf(item, item2, item3, item4)
        viewModel.feedClientOptions.archived = FeedItemArchivedScope.INCLUDE
        viewModel.optimisticallyBulkUpdateStatus(KnockMessageStatusUpdateType.ARCHIVED)
        assertTrue(viewModel.feed.value.meta.unreadCount == 0)
        assertTrue(viewModel.feed.value.entries.size == 4)
    }

    @Test
    fun testOptimisticBulkMarkItemAsArchivedWithReadScope() = runTest {
        val item = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item2 = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item3 = generateTestFeedItem(KnockMessageStatusUpdateType.UNREAD)
        val item4 = generateTestFeedItem(KnockMessageStatusUpdateType.READ)

        viewModel.feed.value.entries = listOf(item, item2, item3, item4)
        viewModel.optimisticallyBulkUpdateStatus(KnockMessageStatusUpdateType.ARCHIVED, FeedItemScope.READ)
        assertTrue(viewModel.feed.value.entries.size == 3)
    }
}