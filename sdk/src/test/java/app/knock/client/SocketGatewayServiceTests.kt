package app.knock.client

import app.knock.client.models.sockets.SocketGatewayConnection
import app.knock.client.models.sockets.UnsupportedSocketModeException
import app.knock.client.services.SocketGatewayService
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.time.Instant

class SocketGatewayServiceTests {
    private val service = SocketGatewayService()

    @Test
    fun decodesValidBody() {
        val r = service.mapper.readValue<SocketGatewayConnection>("""{"expires_at":"2026-11-16T19:02:21Z","mode":"websocket","url":"wss://ws1.knock.app/x"}""")
        assertEquals("websocket", r.mode)
        assertEquals("wss://ws1.knock.app/x", r.url)
        assertEquals(Instant.parse("2026-11-16T19:02:21Z"), r.expiresAt)
    }

    @Test
    fun missingUrl() {
        assertThrows(IOException::class.java) {
            service.mapper.readValue<SocketGatewayConnection>("""{"expires_at":"2026-11-16T19:02:21Z","mode":"websocket"}""")
        }
    }

    @Test
    fun badExpiresAt() {
        assertThrows(IOException::class.java) {
            service.mapper.readValue<SocketGatewayConnection>("""{"expires_at":"nope","mode":"websocket","url":"wss://a"}""")
        }
    }

    @Test
    fun emptyBody() {
        assertThrows(IOException::class.java) {
            service.mapper.readValue<SocketGatewayConnection>("")
        }
    }

    @Test
    fun unsupportedMode() {
        assertThrows(UnsupportedSocketModeException::class.java) {
            service.validate(SocketGatewayConnection("sse", "wss://a", Instant.now()))
        }
    }

    @Test
    fun supportedMode() {
        val connection = SocketGatewayConnection("websocket", "wss://a", Instant.now())
        assertEquals(connection, service.validate(connection))
    }
}
