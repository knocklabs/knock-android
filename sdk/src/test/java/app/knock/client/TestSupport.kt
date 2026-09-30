// Shared helpers for JVM unit tests: a mocked Android Context, a MockWebServer-backed Knock instance
// and an in-process Phoenix websocket server.
package app.knock.client

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.mockito.Mockito
import java.util.concurrent.CopyOnWriteArrayList

internal val json = jacksonObjectMapper()

fun mockContext(): Context {
    val ctx = Mockito.mock(Context::class.java)
    val res = Mockito.mock(Resources::class.java)
    val prefs = FakePrefs()
    Mockito.`when`(ctx.applicationContext).thenReturn(ctx)
    Mockito.`when`(ctx.resources).thenReturn(res)
    Mockito.`when`(ctx.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(prefs)
    return ctx
}

/** (Re)configures the Knock singleton against [server] and "signs in" without touching Firebase. */
fun setupKnock(server: MockWebServer, userId: String? = "u1", userToken: String? = null) {
    Knock.setup(
        mockContext(),
        publishableKey = "pk_test",
        pushChannelId = "push_channel",
        options = KnockStartupOptions(server.url("/").toString().trimEnd('/'), KnockLoggingOptions.NONE)
    )
    Knock.shared.environment.setUserId(userId)
    Knock.shared.environment.setUserToken(userToken)
    Knock.shared.feedManager = null
}

fun feedItemJson(cursor: String, id: String = cursor, blocks: List<Map<String, Any?>> = emptyList()) = mapOf(
    "__cursor" to cursor,
    "id" to id,
    "activities" to emptyList<Any>(),
    "actors" to emptyList<Any>(),
    "blocks" to blocks,
    "data" to null,
    "inserted_at" to "2026-01-01T00:00:00Z",
    "updated_at" to "2026-01-01T00:00:00Z",
    "read_at" to null,
    "seen_at" to null,
    "archived_at" to null,
    "interacted_at" to null,
    "total_activities" to 1,
    "total_actors" to 1,
    "tenant" to null,
)

fun feedJson(items: List<Map<String, Any?>>, after: String? = null, unread: Int = items.size) = json.writeValueAsString(
    mapOf(
        "entries" to items,
        "meta" to mapOf("total_count" to items.size, "unread_count" to unread, "unseen_count" to items.size),
        "page_info" to mapOf("before" to null, "after" to after, "page_size" to 50),
    )
)

fun jsonResponse(body: String, code: Int = 200) =
    MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

/**
 * Minimal Phoenix V2 (JSON array) websocket server: acks joins/heartbeats/leaves and lets tests push events.
 */
class FakePhoenix : WebSocketListener() {
    val joins = CopyOnWriteArrayList<List<Any?>>()
    val sockets = CopyOnWriteArrayList<WebSocket>()
    val connections = java.util.concurrent.atomic.AtomicInteger()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connections.incrementAndGet()
        sockets.add(webSocket)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val msg: List<Any?> = json.readValue(text)
        val (joinRef, ref, topic, event) = msg
        when (event) {
            "phx_join" -> joins.add(msg)
            "heartbeat", "phx_leave" -> Unit
            else -> return
        }
        webSocket.send(
            json.writeValueAsString(listOf(joinRef, ref, topic, "phx_reply", mapOf("status" to "ok", "response" to emptyMap<String, Any>())))
        )
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        sockets.remove(webSocket)
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        sockets.remove(webSocket)
    }

    fun push(topic: String, event: String, payload: Map<String, Any?> = emptyMap()) {
        val frame = json.writeValueAsString(listOf(null, null, topic, event, payload))
        sockets.forEach { it.send(frame) }
    }
}

/** Dispatcher that upgrades `/ws/...` to [phoenix] and delegates everything else to [http]. */
class RoutingDispatcher(
    private val phoenix: FakePhoenix,
    private val http: (RecordedRequest) -> MockResponse,
) : Dispatcher() {
    val httpRequests = CopyOnWriteArrayList<RecordedRequest>()

    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.path?.startsWith("/ws/") == true) return MockResponse().withWebSocketUpgrade(phoenix)
        httpRequests.add(request)
        return http(request)
    }
}

fun waitUntil(timeoutMs: Long = 5_000, cond: () -> Boolean): Boolean {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (cond()) return true
        Thread.sleep(25)
    }
    return cond()
}

class FakePrefs : SharedPreferences {
    private val map = java.util.concurrent.ConcurrentHashMap<String, Any>()
    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?) = map[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = (map[key] as? Set<String>)?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int) = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long) = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float) = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean) = map[key] as? Boolean ?: defValue
    override fun contains(key: String?) = map.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        override fun putString(key: String, value: String?) = apply { if (value == null) map.remove(key) else map[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { if (values == null) map.remove(key) else map[key] = values.toSet() }
        override fun putInt(key: String, value: Int) = apply { map[key] = value }
        override fun putLong(key: String, value: Long) = apply { map[key] = value }
        override fun putFloat(key: String, value: Float) = apply { map[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { map[key] = value }
        override fun remove(key: String) = apply { map.remove(key) }
        override fun clear() = apply { map.clear() }
        override fun commit() = true
        override fun apply() = Unit
    }
}
