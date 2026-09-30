package app.knock.client

import app.knock.client.modules.registerTokenForFCM
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class PushTokenTests {
    private lateinit var server: MockWebServer
    private val devicesOnServer = mutableListOf<Map<String, Any?>>()
    private val puts = AtomicInteger()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = synchronized(devicesOnServer) {
                when (request.method) {
                    "GET" -> if (devicesOnServer.isEmpty()) jsonResponse("{}", 404)
                    else jsonResponse(json.writeValueAsString(mapOf("channel_id" to "push_channel", "data" to mapOf("devices" to devicesOnServer))))
                    "PUT" -> {
                        puts.incrementAndGet()
                        val body: Map<String, Any?> = json.readValue(request.body.readUtf8())
                        @Suppress("UNCHECKED_CAST")
                        val devices = (body["data"] as Map<String, Any?>)["devices"] as List<Map<String, Any?>>
                        devicesOnServer.clear()
                        devicesOnServer.addAll(devices)
                        jsonResponse(json.writeValueAsString(mapOf("channel_id" to "push_channel", "data" to body["data"])))
                    }
                    else -> MockResponse().setResponseCode(500)
                }
            }
        }
        server.start()
        setupKnock(server)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun tokensOnServer() = synchronized(devicesOnServer) { devicesOnServer.map { it["token"] } }

    @Test
    fun rotatedTokenReplacesThePreviousOne() = runBlocking {
        Knock.shared.registerTokenForFCM("push_channel", "token-1")
        Knock.shared.registerTokenForFCM("push_channel", "token-2")
        assertEquals(listOf("token-2"), tokensOnServer())
    }

    @Test
    fun reRegisteringTheSameTokenKeepsItAndDoesNotRewrite() = runBlocking {
        Knock.shared.registerTokenForFCM("push_channel", "token-1")
        Knock.shared.registerTokenForFCM("push_channel", "token-1")
        assertEquals(listOf("token-1"), tokensOnServer())
        assertEquals(1, puts.get())
    }

    @Test
    fun otherDevicesOfTheUserAreKept() = runBlocking {
        synchronized(devicesOnServer) { devicesOnServer.add(mapOf("token" to "other-device", "locale" to "en-US", "timezone" to "UTC")) }
        Knock.shared.registerTokenForFCM("push_channel", "token-1")
        Knock.shared.registerTokenForFCM("push_channel", "token-2")
        assertEquals(listOf("other-device", "token-2"), tokensOnServer())
    }

    @Test
    fun currentTokenStoredAsPreviousByOlderVersionsIsNotRemoved() = runBlocking {
        // SDK <= 1.2.5 recorded the *current* token in the previous-tokens list.
        Knock.shared.environment.getSafeSharedPreferences().edit().putStringSet("knock_previous_push_token", setOf("token-1")).apply()
        Knock.shared.registerTokenForFCM("push_channel", "token-1")
        assertEquals(listOf("token-1"), tokensOnServer())
    }
}
