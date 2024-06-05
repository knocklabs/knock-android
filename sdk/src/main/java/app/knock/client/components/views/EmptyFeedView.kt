package app.knock.client.components.views

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
import app.knock.client.R

@Composable
fun EmptyFeedView(config: EmptyFeedViewConfig, refreshAction: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        config.icon?.let { icon ->
            Image(
                painter = rememberVectorPainter(image = icon),
                contentDescription = null,
                modifier = Modifier.then(config.iconSize?.let { Modifier.size(it.dp) } ?: Modifier)

            )
        } ?: config.iconResId?.let { iconResId ->
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.then(config.iconSize?.let { Modifier.size(it.dp) } ?: Modifier)
            )
        }

        config.title?.let { title ->
            Text(
                text = title,
                style = config.titleTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 170.dp)
            )
        }

        config.subtitle?.let { subtitle ->
            Text(
                text = subtitle,
                style = config.subtitleTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 170.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyFeedView() {
    val config = EmptyFeedViewConfig(
        context = LocalContext.current,
        title = "No Items",
        subtitle = "Please check back later.",
        icon = null,
        iconResId = R.drawable.archive
    )
    EmptyFeedView(config = config) {}
}

data class EmptyFeedViewConfig(
    val context: Context,
    val title: String? = null,
    val titleTextStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 14.sp, fontWeight = FontWeight.Medium),
    val subtitle: String? = null,
    val subtitleTextStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 14.sp),
    val icon: ImageVector? = null,
    val iconResId: Int? = null,
    val iconSize: Int? = null
)