package app.knock.client

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.models.FeedTopActionButtonType
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.FeedManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.time.ZonedDateTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class InAppFeedViewModelNetworkTests {
    private lateinit var server: MockWebServer
    private lateinit var phoenix: FakePhoenix
    private val mainExecutor = Executors.newSingleThreadExecutor()
    private val main = mainExecutor.asCoroutineDispatcher()
    private val uncaught = CopyOnWriteArrayList<Throwable>()
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(main)
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaught.add(e) }
        server = MockWebServer()
        phoenix = FakePhoenix()
        server.start()
        setupKnock(server)
    }

    @After
    fun tearDown() {
        Knock.shared.feedManager?.disconnectFromFeed()
        Knock.shared.feedManager = null
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        Dispatchers.resetMain()
        mainExecutor.shutdownNow()
        server.shutdown()
    }

    private fun newViewModel(): InAppFeedViewModel {
        val ctx = mockContext()
        val filters = listOf(FeedItemScope.ALL, FeedItemScope.UNREAD, FeedItemScope.ARCHIVED).map { InAppFeedFilter(ctx, it) }
        return InAppFeedViewModel(FeedClientOptions(), filters.first(), filters, listOf(FeedTopActionButtonType.MarkAllAsRead()))
    }

    private fun item(id: String) = FeedItem(
        feedCursor = id, actors = emptyList(), blocks = emptyList(), id = id,
        insertedAt = ZonedDateTime.now(), readAt = null, totalActivities = 1, totalActors = 1,
    )

    private fun feedRoutes(handler: (before: String?, after: String?, req: RecordedRequest) -> MockResponse): RoutingDispatcher =
        RoutingDispatcher(phoenix) { req ->
            val url = req.requestUrl!!
            if (url.encodedPath.endsWith("/settings")) jsonResponse("""{"features":{"branding_required":false}}""")
            else handler(url.queryParameter("before"), url.queryParameter("after"), req)
        }.also { server.dispatcher = it }

    @Test
    fun rowTapMarksTheItemInteractedAndRead() = runBlocking(main) {
        Knock.shared.feedManager = FeedManager("feed1")
        val vm = newViewModel()
        val it = item("m1")
        vm.feed.value.entries = listOf(it)
        vm.feed.value.meta.unreadCount = 1
        server.enqueue(jsonResponse("{}"))

        vm.updateMessageEngagementStatus(it, KnockMessageStatusUpdateType.INTERACTED)

        assertEquals("/v1/messages/m1/interacted", server.takeRequest().path)
        assertNotNull(vm.feed.value.entries.first().interactedAt)
        assertNotNull(vm.feed.value.entries.first().readAt)
        assertEquals(0, vm.feed.value.meta.unreadCount)
    }

    @Test
    fun refreshFailureClearsTheSpinnerAndKeepsTheFilter() = runBlocking(main) {
        Knock.shared.feedManager = FeedManager("feed1")
        val vm = newViewModel()
        vm.feedClientOptions.status = FeedItemScope.ARCHIVED
        server.enqueue(MockResponse().setResponseCode(500))

        vm.refreshFeed(showIndicator = true)

        assertFalse(vm.showRefreshIndicator.value)
        assertEquals(FeedItemScope.ARCHIVED, vm.feedClientOptions.status)
        val url = server.takeRequest().requestUrl!!
        assertEquals("all", url.queryParameter("status"))
        assertEquals("only", url.queryParameter("archived"))
    }

    @Test
    fun archivedFilterPaginatesWithTheSameQuery() {
        val dispatcher = feedRoutes { _, after, _ ->
            if (after == null) jsonResponse(feedJson(listOf(feedItemJson("c1")), after = "c1"))
            else jsonResponse(feedJson(listOf(feedItemJson("c2"))))
        }
        Knock.shared.feedManager = FeedManager("feed1")
        val vm = newViewModel()
        vm.feedClientOptions.status = FeedItemScope.ARCHIVED
        runBlocking(main) { vm.refreshFeed() }
        vm.fetchNewPageOfFeedItems()
        assertTrue(waitUntil { vm.feed.value.entries.size == 2 })

        val page = dispatcher.httpRequests.last().requestUrl!!
        assertEquals("c1", page.queryParameter("after"))
        assertEquals("all", page.queryParameter("status"))
        assertEquals("only", page.queryParameter("archived"))
    }

    @Test
    fun realtimeUpdateAfterPaginationFetchesOnlyNewItems() {
        val dispatcher = feedRoutes { before, after, _ ->
            when {
                before == null && after == null -> jsonResponse(feedJson(listOf("c1", "c2", "c3").map { feedItemJson(it) }, after = "c3"))
                before == null && after == "c3" -> jsonResponse(feedJson(listOf("c4", "c5").map { feedItemJson(it) }))
                before == "c1" && after == null -> jsonResponse(feedJson(listOf(feedItemJson("c0"))))
                else -> MockResponse().setResponseCode(400)
            }
        }
        Knock.shared.feedManager = FeedManager("feed1")
        val vm = newViewModel()

        vm.connectFeedAndObserveNewMessages()
        assertTrue(waitUntil { phoenix.joins.size == 1 && vm.feed.value.entries.size == 3 })
        vm.fetchNewPageOfFeedItems()
        assertTrue(waitUntil { vm.feed.value.entries.size == 5 })

        Thread.sleep(200)
        phoenix.push("feeds:feed1:u1", "new-message")
        assertTrue(waitUntil { vm.feed.value.entries.size == 6 })

        assertEquals(listOf("c0", "c1", "c2", "c3", "c4", "c5"), vm.feed.value.entries.map { it.id })
        assertEquals("c0", vm.feed.value.pageInfo.before)
        assertTrue(dispatcher.httpRequests.none { it.requestUrl!!.queryParameter("before") != null && it.requestUrl!!.queryParameter("after") != null })
    }

    @Test
    fun failedRealtimeFetchDoesNotCrash() {
        var failRealtime = false
        feedRoutes { before, _, _ ->
            if (before != null && failRealtime) MockResponse().setResponseCode(500)
            else jsonResponse(feedJson(listOf(feedItemJson("c1"))))
        }
        Knock.shared.feedManager = FeedManager("feed1")
        val vm = newViewModel()
        vm.connectFeedAndObserveNewMessages()
        assertTrue(waitUntil { phoenix.joins.size == 1 && vm.feed.value.entries.size == 1 })

        failRealtime = true
        Thread.sleep(200)
        phoenix.push("feeds:feed1:u1", "new-message")
        Thread.sleep(500)
        assertTrue("uncaught: $uncaught", uncaught.isEmpty())
        assertEquals(1, vm.feed.value.entries.size)
    }

    @Test
    fun factoryDefaultFiltersDoNotHoldTheActivityContext() {
        val appContext = mockContext()
        val activityContext = Mockito.mock(Context::class.java)
        Mockito.`when`(activityContext.applicationContext).thenReturn(appContext)
        val vm = ViewModelProvider(ViewModelStore(), InAppFeedViewModelFactory(activityContext))[InAppFeedViewModel::class.java]
        assertTrue(vm.filterOptions.value.all { it.context === appContext })
        assertTrue(vm.currentFilter.value.context === appContext)
    }
}
