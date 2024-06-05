package app.knock.client.components.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
import coil.compose.AsyncImage

@Composable
fun AvatarView(
    imageURLString: String? = null,
    name: String? = null,
    backgroundColor: Color = KnockColor.Gray.gray5(LocalContext.current),
    size: Int = 32,
    style: TextStyle = defaultAvatarViewTextStyle(LocalContext.current)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .background(backgroundColor, CircleShape)
    ) {
        if (imageURLString != null) {
            AsyncImage(
                model = imageURLString,
                contentDescription = null,
                modifier = Modifier.size(size.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = generateInitials(name)
            if (initials != null) {
                Text(
                    text = initials,
                    style = style
                )
            }
        }
    }
}

private fun defaultAvatarViewTextStyle(context: Context): TextStyle {
    return TextStyle(
        color = KnockColor.Gray.gray11(context),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

private fun generateInitials(name: String?): String? {
    return name?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
        ?.joinToString("")
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun AvatarPreviewView() {
//    AvatarView("https://xsgames.co/randomusers/assets/avatars/male/2.jpg", name = "Matt Gardner")
    AvatarView(name = "Matt Gardner")

}