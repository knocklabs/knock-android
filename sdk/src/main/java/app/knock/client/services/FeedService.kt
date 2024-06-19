package app.knock.client.services

import app.knock.client.models.feed.BulkOperation
import app.knock.client.models.feed.Feed
import app.knock.client.models.feed.FeedClientOptions
import app.knock.client.models.feed.FeedItemScope
import app.knock.client.models.feed.FeedSettings
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.models.networking.URLQueryItem

internal class FeedService: KnockAPIService() {
    suspend fun getUserFeedContent(userId: String, feedId: String, options: FeedClientOptions? = null, queryItems: List<URLQueryItem>): Feed {
        return get<Feed>("/users/$userId/feeds/$feedId", queryItems)
    }

    suspend fun makeBulkStatusUpdate(userId: String, feedId: String, type: KnockMessageStatusUpdateType, options: FeedClientOptions): BulkOperation {
        val engagementStatus = if (options.status != null && options.status!! != FeedItemScope.ALL) {
            serializeValueAsString(options.status!!)
        } else {""}

        val tenants = if (options.tenant != null) {
            listOf(options.tenant!!)
        } else { null }

        val body = mapOf(
            "user_ids" to listOf(userId),
            "engagement_status" to engagementStatus,
            "archived" to options.archived,
            "has_tenant" to options.hasTenant,
            "tenants" to tenants,
        )

        val typeValue = serializeValueAsString(type)
        return post<BulkOperation>("/channels/$feedId/messages/bulk/$typeValue", body)
    }

    suspend fun getFeedSettings(userId: String, feedId: String) : FeedSettings {
        return get<FeedSettings>("/users/$userId/feeds/$feedId/settings")
    }
}
