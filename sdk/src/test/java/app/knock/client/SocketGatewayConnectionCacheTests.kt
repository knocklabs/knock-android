package app.knock.client

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import app.knock.client.models.sockets.SocketGatewayConnection
import app.knock.client.modules.SocketGatewayConnectionCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SocketGatewayConnectionCacheTests {
    private var now = 0L
    private var fetches = 0
    private var failNext = false
    private val expiry = Instant.ofEpochMilli(60_000)

    private val cache = SocketGatewayConnectionCache(
        fetch = {
            fetches++
            if (failNext) throw IOException("boom")
            SocketGatewayConnection("websocket", "wss://ws.test/$fetches", expiry)
        },
        now = { now }
    )

    @Before
    fun setUp() {
        Knock.setup(ApplicationProvider.getApplicationContext<Context>(), "pk_test", null)
    }

    @Test
    fun reusesValueBeforeExpiry() {
        cache.get(); now = 30_000; cache.get()
        assertEquals(1, fetches)
    }

    @Test
    fun refetchesInsidePadding() {
        cache.get(); now = 56_000; cache.get()
        assertEquals(2, fetches)
    }

    @Test
    fun refetchesAfterExpiry() {
        cache.get(); now = 61_000; cache.get()
        assertEquals(2, fetches)
    }

    @Test
    fun refetchesAfterInvalidate() {
        cache.get(); cache.invalidate(); cache.get()
        assertEquals(2, fetches)
    }

    @Test
    fun failedFetchCachesNothing() {
        failNext = true
        assertThrows(IOException::class.java) { cache.get() }
        failNext = false
        cache.get(); cache.get()
        assertEquals(2, fetches)
    }
}
