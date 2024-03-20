package app.knock.client

import android.util.Log
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.phoenixframework.Channel
import org.phoenixframework.Message
import org.phoenixframework.Socket
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Block(
    var content: String,
    var name: String,
    var rendered: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedItem(
    @JsonProperty("__cursor") var feedCursor: String,
    var activities: List<KnockActivity>?,
    var actors: List<KnockUser>,
    var blocks: List<Block>,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf(),
    var id: String,
    var insertedAt: ZonedDateTime?,
    var clickedAt: ZonedDateTime?,
    var interactedAt: ZonedDateTime?,
    var linkClickedAt: ZonedDateTime?,
    var readAt: ZonedDateTime?,
    var seenAt: ZonedDateTime?,
    var tenant: String?,
    var totalActivities: Int,
    var totalActors: Int,
    var updatedAt: ZonedDateTime?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KnockActivity(
    var id: String,
    var actor: KnockUser?,
    var recipient: KnockUser?,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var data: Map<String, Any> = hashMapOf(),
    var insertedAt: ZonedDateTime?,
    var updatedAt: ZonedDateTime?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageInfo(
    var before: String? = null,
    var after: String? = null,
    var pageSize: Int = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedMetadata(
    var totalCount: Int = 0,
    var unreadCount: Int = 0,
    var unseenCount: Int = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Feed(
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var entries: List<FeedItem> = listOf(),
    var meta: FeedMetadata = FeedMetadata(),
    var pageInfo: PageInfo = PageInfo(),
)

enum class BulkOperationStatus {
    @JsonProperty("queued") QUEUED,
    @JsonProperty("processing") PROCESSING,
    @JsonProperty("completed") COMPLETED,
    @JsonProperty("failed") FAILED,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BulkOperation(
    var id: String,
    var name: String,
    var status: BulkOperationStatus,
    var estimatedTotalRows: Int,
    var processedRows: Int,
    var startedAt: ZonedDateTime?,
    var completedAt: ZonedDateTime?,
    var failedAt: ZonedDateTime?,
)

enum class FeedItemScope {
    @JsonProperty("all") ALL,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("read") READ,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("seen") SEEN,
}

enum class FeedItemArchivedScope {
    @JsonProperty("include") INCLUDE,
    @JsonProperty("exclude") EXCLUDE,
    @JsonProperty("only") ONLY,
}

enum class BulkChannelMessageStatusUpdateType {
    @JsonProperty("seen") SEEN,
    @JsonProperty("read") READ,
    @JsonProperty("archived") ARCHIVED,
    @JsonProperty("unseen") UNSEEN,
    @JsonProperty("unread") UNREAD,
    @JsonProperty("unarchived") UNARCHIVED,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedClientOptions(
    var before: String? = null,
    var after: String? = null,
    var pageSize: Int? = null,
    var status: FeedItemScope? = null,
    var source: String? = null, // Optionally scope all notifications to a particular source only
    var tenant: String? = null,  // Optionally scope all requests to a particular tenant
    var hasTenant: Boolean? = null, // Optionally scope to notifications with any tenancy or no tenancy
    var archived: FeedItemArchivedScope? = null, // Optionally scope to a given archived status (defaults to `exclude`)
    var triggerData: Map<String, Any>? = null,
) {
    /**
     * Merge new options to the exiting ones, if the new ones are nil, only a copy of `self` will be returned
     *
     * @param options the options to merge with the current struct, if they are nil, only a copy of `self` will be returned
     * @return a new struct of type `FeedClientOptions` with the options passed as the parameter merged into it.
     */
    fun mergeOptions(options: FeedClientOptions? = null): FeedClientOptions {
        // initialize a new `mergedOptions` as a copy of `this`
        val mergedOptions = this.copy()

        // check if the passed options are not nil
        if (options == null) {
            return mergedOptions
        }

        // for each one of the properties `not nil` in the parameter `options`, override the ones in the new struct
        if (options.before != null) {
            mergedOptions.before = options.before
        }
        if (options.after != null) {
            mergedOptions.after = options.after
        }
        if (options.pageSize != null) {
            mergedOptions.pageSize = options.pageSize
        }
        if (options.status != null) {
            mergedOptions.status = options.status
        }
        if (options.source != null) {
            mergedOptions.source = options.source
        }
        if (options.tenant != null) {
            mergedOptions.tenant = options.tenant
        }
        if (options.hasTenant != null) {
            mergedOptions.hasTenant = options.hasTenant
        }
        if (options.archived != null) {
            mergedOptions.archived = options.archived
        }
        if (options.triggerData != null) {
            mergedOptions.triggerData = options.triggerData
        }

        return mergedOptions
    }
}

class FeedManager(
    client: Knock,
    private var feedId: String,
    options: FeedClientOptions = FeedClientOptions()
) {
    private var api: KnockAPI = client.api
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
        val mapper = jacksonObjectMapper()
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