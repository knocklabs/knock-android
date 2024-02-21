package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockMessage
import app.knock.client.KnockMessageStatusBatchUpdateType
import app.knock.client.KnockMessageStatusUpdateType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MessageModule() { // Assume messageService is provided

    private val messageService: MessageService = MessageService()
    suspend fun getMessage(messageId: String): KnockMessage = withContext(Dispatchers.IO) {
        try {
            val message = messageService.getMessage(messageId) // Assume this is a suspend function
            // Log success, adjust logging as per your project setup
            println("DEBUG: getMessage successful for messageId: $messageId")
            message
        } catch (error: Exception) {
            // Log error
            println("ERROR: getMessage failed for messageId: $messageId with error: ${error.localizedMessage}")
            throw error
        }
    }

    suspend fun updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage = withContext(Dispatchers.IO) {
        try {
            val message = messageService.updateMessageStatus(messageId, status) // Assume this is a suspend function
            // Log success
            println("DEBUG: updateMessageStatus successful for messageId: $messageId")
            message
        } catch (error: Exception) {
            // Log error
            println("ERROR: updateMessageStatus failed for messageId: $messageId with error: ${error.localizedMessage}")
            throw error
        }
    }

    suspend fun deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage = withContext(Dispatchers.IO) {
        try {
            val message = messageService.deleteMessageStatus(messageId, status) // Assume this is a suspend function
            // Log success
            println("DEBUG: deleteMessageStatus successful for messageId: $messageId")
            message
        } catch (error: Exception) {
            // Log error
            println("ERROR: deleteMessageStatus failed for messageId: $messageId with error: ${error.localizedMessage}")
            throw error
        }
    }

    suspend fun batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusBatchUpdateType): List<KnockMessage> = withContext(Dispatchers.IO) {
        try {
            val messages = messageService.batchUpdateStatuses(messageIds, status) // Assume this is a suspend function
            // Log success
            println("DEBUG: batchUpdateStatuses successful for messageIds: $messageIds")
            messages
        } catch (error: Exception) {
            // Log error
            println("ERROR: batchUpdateStatuses failed for messageIds: $messageIds with error: ${error.localizedMessage}")
            throw error
        }
    }
}

// deleteMessageStatus with suspend function
suspend fun Knock.deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType): KnockMessage {
    return messageModule.deleteMessageStatus(messageId, status)
}

// deleteMessageStatus with completion handler
fun Knock.deleteMessageStatus(messageId: String, status: KnockMessageStatusUpdateType, completionHandler: (Result<KnockMessage>) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val message = withContext(Dispatchers.IO) { deleteMessageStatus(messageId, status) }
            completionHandler(Result.success(message))
        } catch (e: Exception) {
            completionHandler(Result.failure(e))
        }
    }
}

// batchUpdateStatuses with suspend function
suspend fun Knock.batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusBatchUpdateType): List<KnockMessage> {
    return messageModule.batchUpdateStatuses(messageIds, status)
}

// batchUpdateStatuses with completion handler
fun Knock.batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusBatchUpdateType, completionHandler: (Result<List<KnockMessage>>) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val messages = withContext(Dispatchers.IO) { batchUpdateStatuses(messageIds, status) }
            completionHandler(Result.success(messages))
        } catch (e: Exception) {
            completionHandler(Result.failure(e))
        }
    }
}