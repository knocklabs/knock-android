package app.knock.client.modules

import app.knock.client.KnockMessage
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

    // Implement deleteMessageStatus and batchUpdateStatuses similarly
}