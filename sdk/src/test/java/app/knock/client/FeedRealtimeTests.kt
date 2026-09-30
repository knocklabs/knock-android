package app.knock.client

import android.app.Activity
import app.knock.client.models.KnockException
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItemArchivedScope
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.modules.FeedManager
import app.knock.client.modules.getUserId
import app.knock.client.modules.signIn
import app.knock.client.modules.signOut
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.atomic.AtomicInteger

class FeedRealtimeTests {
    private lateinit var server: MockWebServer
    private lateinit var phoenix: FakePhoenix
    private lateinit var dispatcher: RoutingDispatcher

    @Before
    fun setUp() {
        server = MockWebServer()
        phoenix = FakePhoenix()
        dispatcher = RoutingDispatcher(phoenix) { jsonResponse(feedJson(emptyList())) }
        server.dispatcher = dispatcher
        server.start()
        setupKnock(server)
    }

    @After
    fun tearDown() {
        Knock.shared.feedManager?.disconnectFromFeed()
        Knock.shared.feedManager = null
        server.shutdown()
    }

    private fun topics() = phoenix.joins.map { it[2] }

    private fun pushAndAwait(counter: AtomicInteger, expected: Int, topic: String = "feeds:feed1:u1") {
        Thread.sleep(200) // let the join reply land
        phoenix.push(topic, "new-message")
        assertTrue("expected $expected deliveries, got ${counter.get()}", waitUntil { counter.get() == expected })
    }

    @Test
    fun handlersSurviveDisconnectAndReconnect() {
        val received = AtomicInteger()
        val feedManager = FeedManager("feed1")

        feedManager.connectToFeed()
        feedManager.on("new-message") { received.incrementAndGet() }
        assertTrue(waitUntil { phoenix.joins.size == 1 })
        pushAndAwait(received, 1)

        feedManager.disconnectFromFeed()
        feedManager.connectToFeed()
        assertTrue(waitUntil { phoenix.joins.size == 2 })
        assertTrue(waitUntil { phoenix.sockets.size == 1 })
        pushAndAwait(received, 2)
    }

    @Test
    fun handlersRegisteredBeforeConnectAreApplied() {
        val received = AtomicInteger()
        val feedManager = FeedManager("feed1")
        feedManager.on("new-message") { received.incrementAndGet() }
        feedManager.connectToFeed()
        assertTrue(waitUntil { phoenix.joins.size == 1 })
        pushAndAwait(received, 1)
    }

    @Test
    fun connectingTwiceWithSameOptionsKeepsOneChannel() {
        val feedManager = FeedManager("feed1")
        feedManager.connectToFeed()
        feedManager.connectToFeed()
        assertTrue(waitUntil { phoenix.joins.size == 1 })
        Thread.sleep(300)
        assertEquals(1, phoenix.joins.size)
        assertEquals(1, phoenix.sockets.size)
    }

    @Test
    fun navigatingBetweenActivitiesKeepsTheFeedConnected() {
        val received = AtomicInteger()
        val feedManager = FeedManager("feed1")
        Knock.shared.feedManager = feedManager
        feedManager.connectToFeed(FeedClientOptions(tenant = "team-a"))
        feedManager.on("new-message") { received.incrementAndGet() }
        val activityA = Mockito.mock(Activity::class.java)
        val activityB = Mockito.mock(Activity::class.java)

        Knock.shared.onActivityStarted(activityA)
        assertTrue(waitUntil { phoenix.sockets.size == 1 })
        Knock.shared.onActivityStarted(activityB) // A -> B: B starts before A stops
        Knock.shared.onActivityStopped(activityA)
        Thread.sleep(300)
        assertEquals(1, phoenix.sockets.size)
        assertEquals(1, phoenix.joins.size)

        Knock.shared.onActivityStopped(activityB) // app backgrounded
        assertTrue(waitUntil { phoenix.sockets.isEmpty() })

        Knock.shared.onActivityStarted(activityB) // foregrounded again
        assertTrue(waitUntil { phoenix.joins.size == 2 })
        assertTrue(waitUntil { phoenix.sockets.size == 1 })
        @Suppress("UNCHECKED_CAST")
        assertEquals("team-a", (phoenix.joins.last()[4] as Map<String, Any?>)["tenant"])
        pushAndAwait(received, 1)
    }

    @Test
    fun joinParamsUseApiWireValues() {
        val feedManager = FeedManager("feed1", FeedClientOptions(status = FeedItemScope.UNREAD, archived = FeedItemArchivedScope.INCLUDE))
        feedManager.connectToFeed()
        assertTrue(waitUntil { phoenix.joins.size == 1 })
        @Suppress("UNCHECKED_CAST")
        val payload = phoenix.joins.first()[4] as Map<String, Any?>
        assertEquals("unread", payload["status"])
        assertEquals("include", payload["archived"])
    }

    @Test
    fun switchingUsersRejoinsAndFetchesAsTheNewUser() = runBlocking {
        Knock.shared.environment.setUserId("alice")
        val feedManager = FeedManager("feed1")
        Knock.shared.feedManager = feedManager
        feedManager.connectToFeed()
        assertTrue(waitUntil { topics() == listOf("feeds:feed1:alice") })

        Knock.shared.signIn("bob", "bob_token") // Firebase isn't configured in unit tests
        assertTrue(waitUntil { topics() == listOf("feeds:feed1:alice", "feeds:feed1:bob") })
        assertTrue(waitUntil { phoenix.sockets.size == 1 })

        feedManager.getUserFeedContent()
        val request = dispatcher.httpRequests.last()
        assertEquals("/v1/users/bob/feeds/feed1", request.path)
        assertEquals("bob_token", request.getHeader("X-Knock-User-Token"))
    }

    @Test
    fun signOutDisconnectsTheFeedAndClearsTheUserWithoutFirebase() = runBlocking {
        val feedManager = FeedManager("feed1")
        Knock.shared.feedManager = feedManager
        feedManager.connectToFeed()
        assertTrue(waitUntil { phoenix.sockets.size == 1 })

        Knock.shared.signOut()
        assertNull(Knock.shared.getUserId())
        assertTrue(waitUntil { phoenix.sockets.isEmpty() })

        // The lifecycle must not reconnect a signed-out user.
        Knock.shared.onActivityStarted(Mockito.mock(Activity::class.java))
        Thread.sleep(300)
        assertTrue(phoenix.sockets.isEmpty())
    }

    @Test
    fun signInWithoutFirebaseSucceeds() = runBlocking {
        Knock.shared.environment.setUserId(null)
        Knock.shared.signIn("u2", "token")
        assertEquals("u2", Knock.shared.getUserId())
        assertEquals(0, dispatcher.httpRequests.size)
    }

    @Test
    fun feedRequestsBeforeSignInFailWithUserIdNotSet() = runBlocking {
        Knock.shared.environment.setUserId(null)
        val result = runCatching { FeedManager("feed1").getUserFeedContent() }
        assertTrue(result.exceptionOrNull() is KnockException.UserIdNotSetError)
    }
}
