package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MarkdownContentBlock::class, name = "markdown"),
    JsonSubTypes.Type(value = TextContentBlock::class, name = "text"),
    JsonSubTypes.Type(value = ButtonSetContentBlock::class, name = "button_set")
)
sealed class ContentBlockBase

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("markdown")
data class MarkdownContentBlock(
    val name: String,
    val content: String,
    val rendered: String
) : ContentBlockBase()

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("text")
data class TextContentBlock(
    val name: String,
    val content: String,
    val rendered: String
) : ContentBlockBase()

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("button_set")
data class ButtonSetContentBlock(
    val name: String,
    val buttons: List<BlockActionButton>
) : ContentBlockBase()

@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockActionButton(
    val label: String,
    val name: String,
    val action: String
)
