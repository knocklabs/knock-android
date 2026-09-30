package app.knock.client

import app.knock.client.models.feed.MarkdownContentBlock
import app.knock.client.models.messages.KnockMessageEngagementStatus
import app.knock.client.models.messages.KnockMessageStatus
import app.knock.client.models.preferences.ChannelTypePreferences
import app.knock.client.models.preferences.PreferenceSet
import app.knock.client.modules.FeedManager
import app.knock.client.modules.getMessage
import app.knock.client.modules.getUserPreferences
import app.knock.client.modules.setUserPreferences
import arrow.core.Either
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModelDecodingTests {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        setupKnock(server)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun messageJson(status: String, workflow: String?, engagement: List<String> = listOf("seen")) = json.writeValueAsString(
        mapOf(
            "id" to "m1", "channel_id" to "ch", "recipient" to "u1", "workflow" to workflow, "tenant" to null,
            "status" to status, "engagement_statuses" to engagement,
            "seen_at" to null, "read_at" to null, "interacted_at" to null, "link_clicked_at" to null, "archived_at" to null,
            "source" to mapOf("key" to "wf", "version_id" to "v1"), "data" to null,
        )
    )

    @Test
    fun messageDecodesEveryApiStatusAndFutureValues() = runBlocking {
        val cases = mapOf(
            "bounced" to KnockMessageStatus.BOUNCED,
            "not_sent" to KnockMessageStatus.NOT_SENT,
            "delivered" to KnockMessageStatus.DELIVERED,
            "some_future_status" to KnockMessageStatus.UNKNOWN,
        )
        cases.forEach { (wire, expected) ->
            server.enqueue(jsonResponse(messageJson(wire, "wf")))
            assertEquals(expected, Knock.shared.getMessage("m1").status)
        }
    }

    @Test
    fun messageWithoutWorkflowOrWithUnknownEngagementDecodes() = runBlocking {
        server.enqueue(jsonResponse(messageJson("delivered", null, listOf("read", "some_future_engagement"))))
        val message = Knock.shared.getMessage("m1")
        assertNull(message.workflow)
        assertEquals(listOf(KnockMessageEngagementStatus.READ, KnockMessageEngagementStatus.UNKNOWN), message.engagementStatuses)
    }

    @Test
    fun unknownContentBlockTypesAreSkipped() = runBlocking {
        val markdown = mapOf("type" to "markdown", "name" to "body", "content" to "a", "rendered" to "<p>a</p>")
        val future = mapOf("type" to "image", "name" to "hero", "url" to "https://x")
        server.enqueue(jsonResponse(feedJson(listOf(feedItemJson("c1", blocks = listOf(future, markdown))))))
        val feed = FeedManager("feed1").getUserFeedContent()
        assertEquals(listOf(MarkdownContentBlock("body", "a", "<p>a</p>")), feed.entries.single().blocks)
    }

    @Test
    fun preferencesRoundTripKeepsAllApiFields() = runBlocking {
        val body = """{
            "id":"default",
            "channel_types":{"email":true,"http":false},
            "channels":{"ch_1":false},
            "commercial_subscribed":false,
            "workflows":{"wf":{"channel_types":{"push":false},"channels":{"ch_2":true},
                "conditions":[{"variable":"recipient.plan","operator":"exists","argument":null}]}},
            "categories":{}
        }"""
        server.enqueue(jsonResponse(body))
        server.enqueue(jsonResponse(body))
        val prefs = Knock.shared.getUserPreferences("default")
        assertEquals(Either.Left(false), prefs.channelTypes.http)
        assertEquals(Either.Left(false), prefs.commercialSubscribed)

        Knock.shared.setUserPreferences("default", prefs)
        server.takeRequest()
        val sent: Map<String, Any?> = json.readValue(server.takeRequest().body.readUtf8())
        val received: Map<String, Any?> = json.readValue(body)
        assertEquals(received, sent)
    }

    @Test
    fun unsetOptionalPreferenceFieldsAreNotSent() = runBlocking {
        server.enqueue(jsonResponse("""{"id":"default","channel_types":{"email":true},"workflows":{},"categories":{}}"""))
        Knock.shared.setUserPreferences("default", PreferenceSet(channelTypes = ChannelTypePreferences(email = Either.Left(true))))
        val sent = server.takeRequest().body.readUtf8()
        assertFalse(sent.contains("commercial_subscribed"))
        assertFalse(sent.contains("\"channels\""))
        assertFalse(sent.contains("http"))
        assertTrue(sent.contains("\"email\":true"))
    }
}
