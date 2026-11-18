package app.knock.client

import app.knock.client.models.sockets.SocketGatewayConnection
import app.knock.client.models.sockets.UnsupportedSocketModeException
import app.knock.client.modules.SocketGatewayConnectionCache
import app.knock.client.modules.SocketGatewayInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class SocketGatewayInterceptorTests {
    private val server = MockWebServer().apply { start() }
    private var fetches = 0
    private var fetchError: Throwable? = null

    private val cache = SocketGatewayConnectionCache(fetch = {
        fetches++
        fetchError?.let { throw it }
        SocketGatewayConnection(
            "websocket",
            server.url("/ws/v1/websocket?auth_token=tok&vsn=signed").toString().replace("http", "ws"),
            Instant.now().plusSeconds(600)
        )
    })

    private val client = OkHttpClient.Builder().addInterceptor(SocketGatewayInterceptor(cache)).build()

    @After
    fun tearDown() = server.shutdown()

    private class Listener : WebSocketListener() {
        val events = LinkedBlockingQueue<Any>()
        override fun onOpen(webSocket: WebSocket, response: Response) { events.add("open") }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { events.add(t) }
    }

    private fun connect(): Listener {
        val listener = Listener()
        client.newWebSocket(Request.Builder().url("http://unused.invalid/ws/v1/websocket?vsn=2.0.0&api_key=pk&user_token=ut").build(), listener)
        return listener
    }

    @Test
    fun rewritesUrlAndMergesParams() {
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {}))
        assertEquals("open", connect().events.poll(5, TimeUnit.SECONDS))
        val url = server.takeRequest().requestUrl!!
        assertEquals("/ws/v1/websocket", url.encodedPath)
        assertEquals("tok", url.queryParameter("auth_token"))
        assertEquals("pk", url.queryParameter("api_key"))
        assertEquals("ut", url.queryParameter("user_token"))
        assertEquals(listOf("signed"), url.queryParameterValues("vsn"))
    }

    @Test
    fun twoConnectsShareOneFetch() {
        repeat(2) {
            server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {}))
            assertEquals("open", connect().events.poll(5, TimeUnit.SECONDS))
        }
        assertEquals(1, fetches)
    }

    @Test
    fun failedHandshakeInvalidatesCache() {
        server.enqueue(MockResponse().setResponseCode(500))
        assertTrue(connect().events.poll(5, TimeUnit.SECONDS) is Throwable)
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {}))
        assertEquals("open", connect().events.poll(5, TimeUnit.SECONDS))
        assertEquals(2, fetches)
    }

    @Test
    fun unsupportedModeArrivesAsSameInstance() {
        val error = UnsupportedSocketModeException("polling")
        fetchError = error
        assertSame(error, connect().events.poll(5, TimeUnit.SECONDS))
    }

    @Test
    fun runtimeExceptionSurfacesAsIOException() {
        fetchError = IllegalStateException("bad")
        val t = connect().events.poll(5, TimeUnit.SECONDS)
        assertTrue(t is IOException)
        assertTrue((t as Throwable).cause is IllegalStateException)
    }
}
