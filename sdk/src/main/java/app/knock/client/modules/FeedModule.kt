package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.logError
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

internal class FeedModule(private val feedId: String, private val defaultOptions: FeedClientOptions) {
    private val feedService = FeedService()

    private var socket: Socket
    private var feedChannel: Channel? = null
    private var feedTopic: String
    private val userId: String

    init {
        val base = Knock.shared.environment.getBaseUrl()
        val websocketHostname = base.replace(Regex("^http"), "ws") // default: wss://api.knock.app
        val websocketPath = "$websocketHostname/ws/v1/websocket" // default: wss://api.knock.app/ws/v1/websocket
        val params = hashMapOf(
            "vsn" to "2.0.0",
            "api_key" to Knock.shared.environment.getPublishableKey(),
            "user_token" to (Knock.shared.environment.getUserToken() ?: "")
        )

        this.socket = Socket(websocketPath, params)
        this.userId = Knock.shared.environment.getSafeUserId()
        this.feedTopic = "feeds:$feedId:$userId"
    }

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
            URLQueryItem("trigger_data", triggerDataJsonString)
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

    fun connectToFeed(options: FeedClientOptions? = null) {
        // Setup the socket to receive open/close events
        socket.logger = {
            Knock.shared.logDebug(KnockLogCategory.FEED, it)
        }
        socket.onOpen {
            Knock.shared.logDebug(KnockLogCategory.FEED, "Socket Opened")
        }
        socket.onClose {
            Knock.shared.logDebug(KnockLogCategory.FEED, "Socket Closed")
        }
        socket.onError { throwable, response ->
            Knock.shared.logError(KnockLogCategory.FEED, "Socket Error ${response?.code}", description = throwable.message)
        }

        val mergedOptions = defaultOptions.mergeOptions(options)
        val params = paramsFromOptions(mergedOptions)

        // Setup the Channel to receive and send messages
        val channel = socket.channel(feedTopic, params)

        // Now connect the socket and join the channel
        this.feedChannel = channel
        this.feedChannel?.join()?.receive("ok") {
            Knock.shared.logDebug(KnockLogCategory.FEED, "CHANNEL: ${channel.topic} joined")
        }?.receive("error") {
            Knock.shared.logError(KnockLogCategory.FEED, "CHANNEL: ${channel.topic} failed to join", description = it.payload.toString())
        }

        socket.connect()
    }

    fun disconnectFromFeed() {
        Knock.shared.logDebug(KnockLogCategory.FEED, "Disconnecting from feed")

        if (feedChannel != null) {
            val channel = feedChannel!!
            channel.leave()
            socket.remove(channel)
        }

        socket.disconnect()
    }

    fun on(eventName: String, callback: (Message) -> Unit) {
        feedChannel?.let {
            val channel = it
            channel.on(eventName, callback)
        } ?: {
            Knock.shared.logError(KnockLogCategory.FEED, "Feed channel is null. You should call first connectToFeed()")
        }
    }

    private fun paramsFromOptions(options: FeedClientOptions): Map<String, Any> {
        val params: MutableMap<String, Any> = mutableMapOf()

        options.before?.let { params["before"] = it }
        options.after?.let { params["after"] = it }
        options.pageSize?.let { params["page_size"] = it }
        options.status?.let { params["status"] = it }
        options.source?.let { params["source"] = it }
        options.tenant?.let { params["tenant"] = it }
        options.hasTenant?.let { params["has_tenant"] = it }
        options.archived?.let { params["archived"] = it }
        options.triggerData?.let { params["trigger_data"] = it }

        return params
    }
}
