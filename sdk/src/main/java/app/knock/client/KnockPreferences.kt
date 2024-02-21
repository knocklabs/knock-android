package app.knock.client

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize




//fun Knock.getAllUserPreferences(completionHandler: (Result<List<PreferenceSet>>) -> Unit) {
//    api.decodeFromGet("/users/$userId/preferences", null, completionHandler)
//}
//
//fun Knock.getUserPreferences(preferenceId: String, completionHandler: (Result<PreferenceSet>) -> Unit) {
//    api.decodeFromGet("/users/$userId/preferences/$preferenceId", null, completionHandler)
//}
//
//fun Knock.setUserPreferences(preferenceId: String, preferenceSet: PreferenceSet, completionHandler: (Result<PreferenceSet>) -> Unit) {
//    api.decodeFromPut("/users/$userId/preferences/$preferenceId", preferenceSet, completionHandler)
//}