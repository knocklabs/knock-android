package app.knock.client.components.themes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InAppFeedNotificationIconButtonTheme(
    val buttonImage: ImageVector = Icons.Filled.Notifications,
    val buttonImageTint: Color = Color.Gray,
    val buttonImageSize: Dp = 24.dp,
    val showBadgeWithCount: Boolean = true,
    val badgeCountTextStyle: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, color = Color.White),
    val backgroundColor: Color = Color.Red,
    val notificationCountType: ReadStatusType = ReadStatusType.UNREAD
)

enum class ReadStatusType {
    UNREAD,
    UNSEEN
}