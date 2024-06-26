package app.knock.client.components.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.knock.client.R

@Composable
fun ImageWithText(
    modifier: Modifier = Modifier,
    image: Painter?,
    imageSize: Int = 20,
    imageColor: Color = Color.White,
    contentDescription: String?,
    text: String?,
    textStyle: TextStyle = TextStyle(fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        image?.let {
            Image(
                painter = it,
                contentDescription = contentDescription,
                modifier = Modifier.size(imageSize.dp),
                contentScale = ContentScale.Fit,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(imageColor)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        text?.let {
            Text(
                text = it,
                style = textStyle
            )
        }
    }
}

@Composable
@Preview
fun PreviewImageWithText() {
    MaterialTheme {
        ImageWithText(
            image = painterResource(id = R.drawable.archive),
            contentDescription = "Example Image",
            text = "Archive",
            modifier = Modifier.padding(16.dp)
        )
    }
}