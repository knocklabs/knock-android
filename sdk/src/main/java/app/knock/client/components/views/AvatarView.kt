package app.knock.client.components.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.knock.client.components.themes.AvatarViewTheme
import coil.compose.AsyncImage

@Composable
fun AvatarView(
    imageURLString: String? = null,
    name: String? = null,
    theme: AvatarViewTheme = AvatarViewTheme(LocalContext.current)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(theme.avatarViewSize.dp)
            .background(theme.avatarViewBackgroundColor, CircleShape)
    ) {
        if (imageURLString != null) {
            AsyncImage(
                model = imageURLString,
                contentDescription = null,
                modifier = Modifier
                    .size(theme.avatarViewSize.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = generateInitials(name)
            if (initials != null) {
                Text(
                    text = initials,
                    style = theme.avatarInitialsTextStyle
                )
            }
        }
    }
}

private fun generateInitials(name: String?): String? {
    return name?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
        ?.joinToString("")
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun AvatarPreviewView() {
    AvatarView(name = "Matt Gardner")
}