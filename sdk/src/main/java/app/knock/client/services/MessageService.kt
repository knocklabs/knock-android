package app.knock.client.services

import app.knock.client.models.messages.KnockMessage
import app.knock.client.models.messages.KnockMessageStatusUpdateType

internal class MessageService: KnockAPIService() {
    suspend fun getMessage(messageId: String): KnockMessage {
        return get<KnockMessage>("/messages/$messageId")
    }

    suspend fun updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
        val statusValue = serializeValueAsString(status)
        return put<KnockMessage>("/messages/$messageId/$statusValue")
    }

    suspend fun deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
        val statusValue = serializeValueAsString(status)
        return delete<KnockMessage>("/messages/$messageId/$statusValue")
    }

    suspend fun batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusUpdateType): List<KnockMessage> {
        val statusValue = serializeValueAsString(status)
        val body = mapOf(
            "message_ids" to messageIds
        )
        return post<List<KnockMessage>>("/messages/batch/$statusValue", body)
    }
}