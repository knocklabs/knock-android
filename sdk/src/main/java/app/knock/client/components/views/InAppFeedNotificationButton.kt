package app.knock.client.components.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import app.knock.client.components.themes.InAppFeedNotificationButtonTheme

@Composable
fun InAppFeedNotificationButton(
    modifier: Modifier = Modifier,
    unreadCount: Int,
    theme: InAppFeedNotificationButtonTheme = InAppFeedNotificationButtonTheme(LocalContext.current),
    action: () -> Unit
) {
    val countText = if (theme.showBadgeWithCount) {
        if (unreadCount > 99) "99" else "$unreadCount"
    } else ""

    val showUnreadBadge = unreadCount > 0

    val badgePadding = if (countText.length > 1) 2.dp else 4.dp

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .clickable(onClick = action)
    ) {
        Icon(
            imageVector = theme.buttonImage,
            contentDescription = null,
            modifier = Modifier.size(theme.buttonImageSize),
            tint = theme.buttonImageForeground
        )
        if (showUnreadBadge) {
            Text(
                text = countText,
                style = theme.badgeCountTextStyle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .background(theme.badgeColor, CircleShape)
                    .padding(badgePadding)

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InAppFeedNotificationButtonPreviewView() {
    InAppFeedNotificationButton(Modifier, 14) {}
}