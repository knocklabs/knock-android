package app.knock.client.components.views

import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import app.knock.client.components.KnockColor
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownContentView(html: String, cssString: String? = null) {
    val state = rememberRichTextState()

    val defaultCss = """
        * {
            font-family: -apple-system, sans-serif;
            font-size: 16px;
            color: #1c2024;
        }
        p {
            margin-top: 0px;
            margin-bottom: 10px;
        }
        blockquote p {
            color: #60646c;
            margin-top: 0px;
            margin-bottom: 10px;
        }
    """.trimIndent()

    val css = cssString ?: defaultCss

    val htmlContent = """
        <html>
            <head>
                <style>
                    $css
                </style>
            </head>
            <body>
                $html
            </body>
        </html>
    """.trimIndent()

//    state.setHtml(htmlContent)
//
//    RichTextEditor(
//        state = state,
//    )

    MarkdownText(html)
}

//MarkdownText(
//html,
//style = TextStyle(fontSize = 16.sp, color = KnockColor.Accent.accent9(LocalContext.current))
//)

fun getCssString(): String {
    val textColor = "#1c2024"
    val blockquoteColor = "#60646c"
    return """
        * {
            font-family: -apple-system, sans-serif;
            font-size: 16px;
            color: $textColor;
        }
        p {
            margin-top: 0px;
            margin-bottom: 10px;
        }
        blockquote p {
            color: $blockquoteColor;
            margin-top: 0px;
            margin-bottom: 10px;
        }
    """.trimIndent()
}