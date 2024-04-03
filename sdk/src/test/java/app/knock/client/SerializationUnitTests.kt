package app.knock.client

import app.knock.client.models.feed.ButtonSetContentBlock
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.MarkdownContentBlock
import app.knock.client.models.feed.TextContentBlock
import app.knock.client.models.preferences.ChannelTypePreferences
import app.knock.client.models.preferences.PreferenceSet
import com.fasterxml.jackson.databind.ObjectMapper
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
class SerializationUnitTests {
    @Test
    fun testDefaultsForNullValues() {
        val mapper = jacksonObjectMapper()
        mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        mapper.registerModule(JavaTimeModule())
        mapper.registerKotlinModule()

        val rawJson = """
            [
                {
                    "__typename": "PreferenceSet",
                    "categories": null,
                    "channel_types": null,
                    "id": "default",
                    "workflows": null
                }
            ]
        """.trimIndent()

        val expectedResult = listOf(PreferenceSet("default", ChannelTypePreferences(), mapOf(), mapOf()))

        try {
            val decodedObject: List<PreferenceSet> = mapper.readValue(rawJson)
            Assert.assertEquals(expectedResult, decodedObject)
        } catch (e: Exception) {
            throw AssertionError("Failed to decode object: ${e.localizedMessage}")
        }
    }

    @Test
    fun testContentBlockDecoding() {
        val objectMapper: ObjectMapper = jacksonObjectMapper()

        val jsonString = """
            {
                "__cursor": "g3QAAAABZAALaW5zZXJ0ZWRfYXR0AAAADWQACl",
                "activities": [],
                "actors": [],
                "blocks": [
                    {
                        "content": "asdf",
                        "name": "body",
                        "rendered": "asdf",
                        "type": "markdown"
                    },
                    {
                        "content": "asdf",
                        "name": "body",
                        "rendered": "asdf",
                        "type": "text"
                    },
                    {
                        "buttons": [
                            {
                                "action": "/action-url",
                                "label": "Primary",
                                "name": "primary"
                            }
                        ],
                        "name": "actions",
                        "type": "button_set"
                    }
                ],
                "id": "2e36JCjQAu5trvYtvmW9O14odis",
                "total_activities": 1,
                "total_actors": 1,
                "source": {
                    "key": "in-app",
                    "version_id": "a4c43e19-056d-46f9-b4da-20c55c8c0bf7"
                },
                "tenant": "team-a"
            }
        """.trimIndent()

        val item: FeedItem = objectMapper.readValue(jsonString)
        assert(item.blocks.size == 3)
        assert(item.blocks[0] is MarkdownContentBlock)
        assert(item.blocks[1] is TextContentBlock)
        assert(item.blocks[2] is ButtonSetContentBlock)
    }
}