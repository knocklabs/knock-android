package app.knock.client.components.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownContentView(html: String) {

    val style = TextStyle(
        color = KnockColor.Gray.gray12(LocalContext.current),
        fontSize = 16.sp
    )

    MarkdownText(markdown = html, style = style, disableLinkMovementMethod = true)
}

@Preview(showBackground = true)
@Composable
fun PreviewMarkdownContentView() {
    val markdown1 = "<p>Hey <strong>Dennis</strong> 👋 - Ian Malcolm completed an activity.</p>"
    val markdown2 = "Here's a new notification from <strong>Eleanor Price</strong>:</p><blockquote><p>test message test message test message test message test message test message test message test message test message </p></blockquote>"
    MarkdownContentView(html = markdown1)
}