package app.knock.client

import app.knock.client.models.KnockException
import app.knock.client.modules.getUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class KnockAPIServiceTests {
    private lateinit var server: MockWebServer

    private val userJson = """{"id":"u1","name":"U","email":null,"avatar":null,"phone_number":null}"""

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        setupKnock(server)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun bodyReadFailureFailsInsteadOfHanging() = runBlocking {
        server.enqueue(
            jsonResponse(userJson + " ".repeat(64_000)).setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
        )
        val outcome = withTimeoutOrNull(5_000) { runCatching { Knock.shared.getUser() } }
        assertNotNull("request hung instead of failing", outcome)
        assertTrue(outcome!!.exceptionOrNull() is IOException)
    }

    @Test
    fun errorResponseIsReportedWithReadableBody() = runBlocking {
        server.enqueue(jsonResponse("""{"code":"not_found"}""", 404))
        val error = runCatching { Knock.shared.getUser() }.exceptionOrNull()
        assertTrue(error is KnockException.NetworkError)
        error as KnockException.NetworkError
        assertEquals(404, error.code)
        assertEquals("""{"code":"not_found"}""", error.response?.body?.string())
    }

    @Test
    fun pathParametersArePercentEncoded() = runBlocking {
        Knock.shared.environment.setUserId("team#1/alice?x y")
        server.enqueue(jsonResponse(userJson))
        Knock.shared.getUser()
        assertEquals("/v1/users/team%231%2Falice%3Fx%20y", server.takeRequest().path)
    }

    @Test
    fun cancellingTheCoroutineCancelsTheHttpCall() = runBlocking {
        server.enqueue(jsonResponse(userJson).setHeadersDelay(2, TimeUnit.SECONDS))
        val request = async(Dispatchers.Default) { runCatching { Knock.shared.getUser() } }
        assertTrue(waitUntil { Knock.shared.httpClient.dispatcher.runningCallsCount() == 1 })
        delay(100)
        request.cancel()
        assertTrue(waitUntil(1_000) { Knock.shared.httpClient.dispatcher.runningCallsCount() == 0 })
    }
}
