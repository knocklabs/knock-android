package app.knock.client

import app.knock.client.models.feed.BulkOperation
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.modules.FeedModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phoenixframework.Message

class FeedManager(
    feedId: String,
    options: FeedClientOptions = FeedClientOptions()
) {
    private val feedModule: FeedModule

    init {
        feedModule = FeedModule(feedId, options)
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
        feedModule.connectToFeed(options)
    }

    fun disconnectFromFeed() {
        feedModule.disconnectFromFeed()
    }

    fun on(eventName: String = "new-message", callback: (Message) -> Unit) {
        feedModule.on(eventName, callback)
    }


    /**
     * Retrieves a feed of items in reverse chronological order
     * @param options: [optional] Options of type `FeedClientOptions` to merge with the default ones (set on the constructor) and scope as much as possible the results
     */
    suspend fun getUserFeedContent(options: FeedClientOptions? = null): Feed {
        return feedModule.getUserFeedContent(options)
    }

    fun getUserFeedContent(options: FeedClientOptions? = null, completionHandler: (Result<Feed>) -> Unit)= Knock.coroutineScope.launch(
        Dispatchers.Main) {
        try {
            val feed = withContext(Dispatchers.IO) {
                getUserFeedContent(options)
            }
            completionHandler(Result.success(feed))
        } catch (e: Exception) {
            completionHandler(Result.failure(e))
        }
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
    suspend fun makeBulkStatusUpdate(type: KnockMessageStatusUpdateType, options: FeedClientOptions? = null): BulkOperation {
        return feedModule.makeBulkStatusUpdate(type, options)
    }

    fun makeBulkStatusUpdate(type: KnockMessageStatusUpdateType, options: FeedClientOptions? = null, completionHandler: (Result<BulkOperation>) -> Unit)= Knock.coroutineScope.launch(
        Dispatchers.Main) {
        try {
            val batch = withContext(Dispatchers.IO) {
                makeBulkStatusUpdate(type, options)
            }
            completionHandler(Result.success(batch))
        } catch (e: Exception) {
            completionHandler(Result.failure(e))
        }
    }
}