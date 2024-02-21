package app.knock.client

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime



//sealed class Either<out L, out R>
//data class Left<out L>(val l: L) : Either<L, Nothing>()
//data class Right<out R>(val r: R) : Either<Nothing, R>()
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//data class RecipientIdentifier(
//    var id: String,
//    var collection: String,
//)









//fun Knock.getMessage(messageId: String, completionHandler: (Result<KnockMessage>) -> Unit) {
//    api.decodeFromGet("/messages/$messageId", null, completionHandler)
//}
//
//fun Knock.updateMessageStatus(message: KnockMessage, status: KnockMessageStatusUpdateType, completionHandler: (Result<KnockMessage>) -> Unit) {
//    updateMessageStatus(message.id, status, completionHandler)
//}
//
//fun Knock.updateMessageStatus(messageId: String, status: KnockMessageStatusUpdateType, completionHandler: (Result<KnockMessage>) -> Unit) {
//    val statusValue = api.serializeValueAsString(status)
//    api.decodeFromPut("/messages/$messageId/$statusValue", null, completionHandler)
//}
//
//fun Knock.deleteMessageStatus(message: KnockMessage, status: KnockMessageStatusDeleteType, completionHandler: (Result<KnockMessage>) -> Unit) {
//    deleteMessageStatus(message.id, status, completionHandler)
//}
//
//fun Knock.deleteMessageStatus(messageId: String, status: KnockMessageStatusDeleteType, completionHandler: (Result<KnockMessage>) -> Unit) {
//    val statusValue = api.serializeValueAsString(status)
//    api.decodeFromDelete("/messages/$messageId/$statusValue", null, completionHandler)
//}

// This method that receives (messages: List<KnockMessage>) clashes in the JVM signature with the one that receives (messageIds: List<String>),
// because of that it's now commented. Speculation: Probably the JVM signature is List for both cases
//fun Knock.batchUpdateStatuses(messages: List<KnockMessage>, status: KnockMessageStatusBatchUpdateType, completionHandler: (Result<List<KnockMessage>>) -> Unit) {
//    val messageIds = messages.map { it.id }
//    batchUpdateStatuses(messageIds, status, completionHandler)
//}

/**
 * Batch status update for a list of messages
 *
 * @param messageIds the list of message ids: `[String]` to be updated
 * @param status the new `Status`
 * @param completionHandler the code to execute when the response is received
 */
//fun Knock.batchUpdateStatuses(messageIds: List<String>, status: KnockMessageStatusBatchUpdateType, completionHandler: (Result<List<KnockMessage>>) -> Unit) {
//    val body = mapOf(
//        "message_ids" to messageIds
//    )
//
//    val statusValue = api.serializeValueAsString(status)
//    api.decodeFromPost("/messages/batch/$statusValue", body, completionHandler)
//}