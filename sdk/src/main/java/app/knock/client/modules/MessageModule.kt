package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.models.messages.KnockMessage
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import app.knock.client.services.MessageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MessageModule {

    private val messageService: MessageService = MessageService()
    suspend fun getMessage(messageId: String): KnockMessage {
        return messageService.getMessage(messageId)
    }

    suspend fun updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
        return messageService.updateMessageStatus(messageId, status)
    }

    suspend fun deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
        return messageService.deleteMessageStatus(messageId, status)
    }

    suspend fun batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusUpdateType): List<KnockMessage> {
        return messageService.batchUpdateStatuses(messageIds, status)
    }
}

// Public API Methods

/**
 * Returns the KnockMessage for the associated messageId.
 * https://docs.knock.app/reference#get-a-message
 *
 * @param messageId: The messageId for the KnockMessage.
 */
suspend fun Knock.getMessage(messageId: String): KnockMessage {
    return messageModule.getMessage(messageId)
}

fun Knock.getMessage(messageId: String, completionHandler: (Result<KnockMessage>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val message = withContext(Dispatchers.IO) {
            getMessage(messageId)
        }
        completionHandler(Result.success(message))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}


/**
 * Marks the given message with the provided status, recording an event in the process.
 * https://docs.knock.app/reference#update-message-status
 *
 * @param message: The KnockMessage that you want to update.
 * @param status: The new status to be associated with the KnockMessage.
 */
suspend fun Knock.updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
    return messageModule.updateMessageStatus(messageId, status)
}

fun Knock.updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType, completionHandler: (Result<KnockMessage>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val message = withContext(Dispatchers.IO) {
            updateMessageStatus(messageId, status)
        }
        completionHandler(Result.success(message))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}


/**
 * Un-marks the given status on a message, recording an event in the process.
 * https://docs.knock.app/reference#undo-message-status
 *
 * @param message: The KnockMessage that you want to update.
 * @param status: The new status to be associated with the KnockMessage.
 */
suspend fun Knock.deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
    return messageModule.deleteMessageStatus(messageId, status)
}

fun Knock.deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType, completionHandler: (Result<KnockMessage>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val message = withContext(Dispatchers.IO) {
            deleteMessageStatus(messageId, status)
        }
        completionHandler(Result.success(message))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}


/**
 * Batch status update for a list of messages
 * https://docs.knock.app/reference#batch-update-message-status
 *
 * @param messageIds: the list of message ids: `[String]` to be updated
 * @param status: the new `Status`
 *
 *  *Note:* Knock scopes this batch rate limit by message_ids and status. This allows for 1 update per second per message per status.
 */
suspend fun Knock.batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusUpdateType): List<KnockMessage> {
    return messageModule.batchUpdateStatuses(messageIds, status)
}

fun Knock.batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusUpdateType, completionHandler: (Result<List<KnockMessage>>) -> Unit) = coroutineScope.launch(Dispatchers.Main) {
    try {
        val messages = withContext(Dispatchers.IO) {
            batchUpdateStatuses(messageIds, status)
        }
        completionHandler(Result.success(messages))
    } catch (e: Exception) {
        completionHandler(Result.failure(e))
    }
}