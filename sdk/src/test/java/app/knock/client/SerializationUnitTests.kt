package app.knock.client

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//class SerializationUnitTests {
//    @Test
//    fun testDefaultsForNullValues() {
//        val mapper = jacksonObjectMapper()
//        mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
//        mapper.registerModule(JavaTimeModule())
//        mapper.registerKotlinModule()
//
//        val rawJson = """
//            [
//                {
//                    "__typename": "PreferenceSet",
//                    "categories": null,
//                    "channel_types": null,
//                    "id": "default",
//                    "workflows": null
//                }
//            ]
//        """.trimIndent()
//
//        val expectedResult = listOf(PreferenceSet("default", ChannelTypePreferences(), mapOf(), mapOf()))
//
//        try {
//            val decodedObject: List<PreferenceSet> = mapper.readValue(rawJson)
//            Assert.assertEquals(expectedResult, decodedObject)
//        } catch (e: Exception) {
//            throw AssertionError("Failed to decode object: ${e.localizedMessage}")
//        }
//    }
//}