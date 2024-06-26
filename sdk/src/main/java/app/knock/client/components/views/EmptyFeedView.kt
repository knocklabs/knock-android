package app.knock.client.components.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.knock.client.R
import app.knock.client.components.themes.EmptyFeedViewTheme

@Composable
fun EmptyFeedView(theme: EmptyFeedViewTheme) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        theme.icon?.let { icon ->
            Image(
                painter = rememberVectorPainter(image = icon),
                contentDescription = null,
                modifier = Modifier.then(theme.iconSize?.let { Modifier.size(it.dp) } ?: Modifier),
                colorFilter = ColorFilter.tint(theme.iconColor)
            )
        } ?: theme.iconResId?.let { iconResId ->
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.then(theme.iconSize?.let { Modifier.size(it.dp) } ?: Modifier),
                colorFilter = ColorFilter.tint(theme.iconColor)
            )
        }

        theme.title?.let { title ->
            Text(
                text = title,
                style = theme.titleTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 170.dp)
            )
        }

        theme.subtitle?.let { subtitle ->
            Text(
                text = subtitle,
                style = theme.subtitleTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 170.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyFeedView() {
    val theme = EmptyFeedViewTheme(
        context = LocalContext.current,
        title = "No Items",
        subtitle = "Please check back later.",
        icon = null,
        iconResId = R.drawable.archive
    )
    EmptyFeedView(theme = theme)
}