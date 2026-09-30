package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.logError
import app.knock.client.logWarning
import app.knock.client.models.feed.BulkOperation
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedSettings
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.models.networking.URLQueryItem
import app.knock.client.services.FeedService
import org.phoenixframework.Channel
import org.phoenixframework.Message
import org.phoenixframework.Socket
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.CopyOnWriteArrayList

internal class FeedModule(private val feedId: String, private val defaultOptions: FeedClientOptions) {
    private val feedService = FeedService()

    private val lock = Any()
    private var socket: Socket? = null
    private var feedChannel: Channel? = null
    private var connection: Connection? = null
    private var lastConnectOptions: FeedClientOptions? = null

    // Handlers are kept here and bound to every channel we create, so they survive reconnects.
    private val eventHandlers = CopyOnWriteArrayList<Pair<String, (Message) -> Unit>>()

    private data class Connection(val userId: String, val userToken: String?, val options: FeedClientOptions)

    private val userId: String
        get() = Knock.shared.environment.getSafeUserId()

    suspend fun getUserFeedContent(options: FeedClientOptions? = null): Feed {
        val mapper = jacksonObjectMapper()
        val mergedOptions = defaultOptions.mergeOptions(options)

        val triggerDataJsonString = mergedOptions.triggerData?.let { mapper.writeValueAsString(it) }
        val statusString = mergedOptions.status?.toString()?.lowercase()
        val archivedString = mergedOptions.archived?.toString()?.lowercase()

        val queryItems: List<URLQueryItem> = listOf(
            URLQueryItem("page_size", mergedOptions.pageSize),
            URLQueryItem("after", mergedOptions.after),
            URLQueryItem("before", mergedOptions.before),
            URLQueryItem("source", mergedOptions.source),
            URLQueryItem("tenant", mergedOptions.tenant),
            URLQueryItem("has_tenant", mergedOptions.hasTenant),
            URLQueryItem("status", statusString),
            URLQueryItem("archived", archivedString),
            URLQueryItem("trigger_data", triggerDataJsonString),
            URLQueryItem("locale", mergedOptions.locale)
        )

        return feedService.getUserFeedContent(userId, feedId, mergedOptions, queryItems)
    }

    suspend fun makeBulkStatusUpdate(type: KnockMessageStatusUpdateType, options: FeedClientOptions? = null): BulkOperation {
        val mergedOptions = defaultOptions.mergeOptions(options)
        return feedService.makeBulkStatusUpdate(userId, feedId, type, mergedOptions)
    }

    suspend fun getFeedSettings(): FeedSettings {
        return feedService.getFeedSettings(userId, feedId)
    }

    fun connectToFeed(options: FeedClientOptions? = null) = synchronized(lock) {
        lastConnectOptions = options
        connectLocked()
    }

    fun disconnectFromFeed() = synchronized(lock) {
        Knock.shared.logDebug(KnockLogCategory.FEED, "Disconnecting from feed")
        teardownLocked()
    }

    fun on(eventName: String, callback: (Message) -> Unit) = synchronized(lock) {
        eventHandlers.add(eventName to callback)
        feedChannel?.on(eventName, callback)
        Unit
    }

    /** Reconnects with the options of the last [connectToFeed] call, e.g. when the app returns to the foreground. */
    fun resumeConnection() = synchronized(lock) {
        connectLocked()
    }

    /** Re-joins as the current user if the signed-in user or token changed while connected. */
    fun onUserChanged() = synchronized(lock) {
        if (connection != null) connectLocked()
    }

    private fun connectLocked() {
        val currentUserId = Knock.shared.environment.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            Knock.shared.logWarning(KnockLogCategory.FEED, "Cannot connect to feed until Knock.signIn() is called.")
            teardownLocked()
            return
        }
        val target = Connection(currentUserId, Knock.shared.environment.getUserToken(), defaultOptions.mergeOptions(lastConnectOptions))
        if (target == connection) return

        teardownLocked()

        val socket = createSocket(target.userToken)
        val channel = socket.channel("feeds:$feedId:${target.userId}", paramsFromOptions(target.options))
        eventHandlers.forEach { (eventName, callback) -> channel.on(eventName, callback) }
        channel.join().receive("ok") {
            Knock.shared.logDebug(KnockLogCategory.FEED, "CHANNEL: ${channel.topic} joined")
        }.receive("error") {
            Knock.shared.logError(KnockLogCategory.FEED, "CHANNEL: ${channel.topic} failed to join", description = it.payload.toString())
        }
        socket.connect()

        this.socket = socket
        this.feedChannel = channel
        this.connection = target
    }

    private fun teardownLocked() {
        feedChannel?.let { channel ->
            channel.leave()
            socket?.remove(channel)
        }
        socket?.disconnect()
        feedChannel = null
        socket = null
        connection = null
    }

    private fun createSocket(userToken: String?): Socket {
        val base = Knock.shared.environment.getBaseUrl()
        val websocketHostname = base.replace(Regex("^http"), "ws") // default: wss://api.knock.app
        val websocketPath = "$websocketHostname/ws/v1/websocket" // default: wss://api.knock.app/ws/v1/websocket
        val params = hashMapOf(
            "vsn" to "2.0.0",
            "api_key" to Knock.shared.environment.getPublishableKey(),
            "user_token" to (userToken ?: "")
        )

        return Socket(websocketPath, params).apply {
            logger = { Knock.shared.logDebug(KnockLogCategory.FEED, it) }
            onOpen { Knock.shared.logDebug(KnockLogCategory.FEED, "Socket Opened") }
            onClose { Knock.shared.logDebug(KnockLogCategory.FEED, "Socket Closed") }
            onError { throwable, response ->
                Knock.shared.logError(KnockLogCategory.FEED, "Socket Error ${response?.code}", description = throwable.message)
            }
        }
    }

    private fun paramsFromOptions(options: FeedClientOptions): Map<String, Any> {
        val params: MutableMap<String, Any> = mutableMapOf()

        options.before?.let { params["before"] = it }
        options.after?.let { params["after"] = it }
        options.pageSize?.let { params["page_size"] = it }
        options.status?.let { params["status"] = feedService.serializeValueAsString(it) }
        options.source?.let { params["source"] = it }
        options.tenant?.let { params["tenant"] = it }
        options.hasTenant?.let { params["has_tenant"] = it }
        options.archived?.let { params["archived"] = feedService.serializeValueAsString(it) }
        options.triggerData?.let { params["trigger_data"] = it }

        return params
    }
}
