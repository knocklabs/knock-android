package app.knock.client.components.views

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import app.knock.client.components.KnockColor
import app.knock.client.models.feed.MarkdownContentBlock
import com.google.android.material.textview.MaterialTextView
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownContentView(html: String) {

    val style = TextStyle(
        color = KnockColor.Gray.gray12(LocalContext.current),
        fontSize = 16.sp
    )

//    AndroidView(
//        factory = {
//            MaterialTextView(it).apply {
//                // links
//                autoLinkMask = Linkify.WEB_URLS
//                linksClickable = true
////                setLinkTextColor(Color.White.toArgb())
//            }
//        },
//        update = {
//            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE)
//        }
//    )

//    AndroidView(
//        factory = { context -> TextView(context) },
//        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE) }
//    )

    MarkdownText(markdown = html, style = style)
}

@Preview(showBackground = true)
@Composable
fun PreviewMarkdownContentView() {
    val markdown1 = "<p>Hey <strong>Dennis</strong> 👋 - Ian Malcolm completed an activity.</p>"
    val markdown2 = "<p>Here's a new notification from <strong>Eleanor Price</strong>:</p><blockquote><p>test message test message test message test message test message test message test message test message test message </p></blockquote>"
    MarkdownContentView(html = markdown2)
}