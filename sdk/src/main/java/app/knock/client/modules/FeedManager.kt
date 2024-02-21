package app.knock.client.modules

import android.util.Log
import app.knock.client.Knock
import app.knock.client.services.KnockAPIService
import app.knock.client.services.URLQueryItem
import app.knock.client.models.feed.BulkChannelMessageStatusUpdateType
import app.knock.client.models.feed.BulkOperation
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItemScope
import org.phoenixframework.Channel
import org.phoenixframework.Message
import org.phoenixframework.Socket

class FeedManager(
    client: Knock,
    private var feedId: String,
    options: FeedClientOptions = FeedClientOptions()
) {
    private var api: KnockAPIService = client.api
    private var socket: Socket
    private var feedChannel: Channel? = null
    private var userId: String = client.userId
    private var feedTopic: String = "feeds:$feedId:${client.userId}"
    private var defaultFeedOptions: FeedClientOptions = options
    private val loggerTag = "app.knock.kotlin_client"

    init {
        // use regex and circumflex accent to mark only the starting http to be replaced and not any others
        val websocketHostname = client.api.hostname.replace(Regex("^http"), "ws") // default: wss://api.knock.app
        val websocketPath = "$websocketHostname/ws/v1/websocket" // default: wss://api.knock.app/ws/v1/websocket
        val params = hashMapOf(
            "vsn" to "2.0.0",
            "api_key" to client.publishableKey,
            "user_token" to (client.userToken ?: "")
        )
        this.socket = Socket(websocketPath, params)
    }

    /**
     * Connect to the feed via socket.
     *
     * This will initialize the connection.
     *
     * You should also call the `on(eventName, completionHandler)` function to delegate what should
     * be executed on certain received events and the `disconnectFromFeed()` function to terminate the connection.
     *
     * @param options options of type `FeedClientOptions` to merge with the default ones (set on the constructor)
     * and scope as much as possible the results
     */
    fun connectToFeed(options: FeedClientOptions? = null) {
        // Setup the socket to receive open/close events
        socket.logger = {
            Log.d(loggerTag, it)
        }
        socket.onOpen {
            Log.d(loggerTag, "Socket Opened")
        }
        socket.onClose {
            Log.d(loggerTag, "Socket Closed")
        }
        socket.onError { throwable, response ->
            Log.d(loggerTag, "Socket Error ${response?.code}", throwable)
        }

        val mergedOptions = defaultFeedOptions.mergeOptions(options)
        val params = paramsFromOptions(mergedOptions)

        // Setup the Channel to receive and send messages
        val channel = socket.channel(feedTopic, params)

        // Now connect the socket and join the channel
        this.feedChannel = channel
        this.feedChannel?.join()?.receive("ok") {
            Log.d(loggerTag, "CHANNEL: ${channel.topic} joined")
        }?.receive("error") {
            Log.d(loggerTag, "CHANNEL: ${channel.topic} failed to join. ${it.payload}")
        }

        socket.connect()
    }

    fun on(eventName: String, callback: (Message) -> Unit) {
        if (this.feedChannel != null) {
            val channel = this.feedChannel!!
            channel.on(eventName, callback)
        }
        else {
            Log.d(loggerTag, "Feed channel is nil. You should call first connectToFeed()")
        }
    }

    fun disconnectFromFeed() {
        Log.d(loggerTag, "Disconnecting from feed")

        if (feedChannel != null) {
            val channel = feedChannel!!
            channel.leave()
            socket.remove(channel)
        }

        socket.disconnect()
    }

    /**
     * Gets the content of the user feed
     *
     * @param options options of type `FeedClientOptions` to merge with the default
     * ones (set on the constructor) and scope as much as possible the results
     * @param completionHandler the code to execute when the response is received
     */
    fun getUserFeedContent(options: FeedClientOptions? = null, completionHandler: (Result<Feed>) -> Unit) {
        val mergedOptions = defaultFeedOptions.mergeOptions(options)

        val queryItems: List<URLQueryItem> = listOf(
            URLQueryItem("page_size", mergedOptions.pageSize),
            URLQueryItem("after", mergedOptions.after),
            URLQueryItem("before", mergedOptions.before),
            URLQueryItem("source", mergedOptions.source),
            URLQueryItem("tenant", mergedOptions.tenant),
            URLQueryItem("has_tenant", mergedOptions.hasTenant),
            URLQueryItem("status", mergedOptions.status),
            URLQueryItem("archived", mergedOptions.archived),
            URLQueryItem("trigger_data", mergedOptions.triggerData),
        )

        api.decodeFromGet("/users/$userId/feeds/$feedId", queryItems, completionHandler)
    }

    /**
     * Updates feed messages in bulk
     *
     * **Attention:** : The base scope for the call should take into account all of the options
     * currently set on the feed, as well as being scoped for the current user.
     * We do this so that we **ONLY** make changes to the messages that are currently in view on
     * this feed, and not all messages that exist.
     *
     * @param type the kind of update
     * @param options all the options currently set on the feed to scope as much as possible the bulk update
     * @param completionHandler the code to execute when the response is received
     */
    fun makeBulkStatusUpdate(type: BulkChannelMessageStatusUpdateType, options: FeedClientOptions, completionHandler: (Result<BulkOperation>) -> Unit) {
        // TODO: check https://docs.knock.app/reference#bulk-update-channel-message-status
        // older_than: ISO-8601, check milliseconds
        // newer_than: ISO-8601, check milliseconds
        // delivery_status: one of `queued`, `sent`, `delivered`, `delivery_attempted`, `undelivered`, `not_sent`
        // engagement_status: one of `seen`, `unseen`, `read`, `unread`, `archived`, `unarchived`, `interacted`
        // Also check if the parameters sent here are valid

        val engagementStatus = if (options.status != null && options.status!! != FeedItemScope.ALL) {
            api.serializeValueAsString(options.status!!)
        }
        else {
            ""
        }

        val tenants = if (options.tenant != null) {
            listOf(options.tenant!!)
        }
        else {
            null
        }

        val body = mapOf(
            "user_ids" to listOf(userId),
            "engagement_status" to engagementStatus,
            "archived" to options.archived,
            "has_tenant" to options.hasTenant,
            "tenants" to tenants,
        )

        val typeValue = api.serializeValueAsString(type)
        api.decodeFromPost("/channels/$feedId/messages/bulk/$typeValue", body, completionHandler)
    }

    private fun paramsFromOptions(options: FeedClientOptions): Map<String, Any> {
        val params: Map<String, Any> = hashMapOf()

        if (options.before != null) {
            params["before"] to options.before!!
        }

        if (options.after != null) {
            params["after"] to options.after!!
        }

        if (options.pageSize != null) {
            params["page_size"] to options.pageSize!!
        }

        if (options.status != null) {
            params["status"] to options.status!!
        }

        if (options.source != null) {
            params["source"] to options.source!!
        }

        if (options.tenant != null) {
            params["tenant"] to options.tenant!!
        }

        if (options.hasTenant != null) {
            params["has_tenant"] to options.hasTenant!!
        }

        if (options.archived != null) {
            params["archived"] to options.archived!!
        }

        if (options.triggerData != null) {
            params["trigger_data"] to options.triggerData!!
        }

        return params
    }
}