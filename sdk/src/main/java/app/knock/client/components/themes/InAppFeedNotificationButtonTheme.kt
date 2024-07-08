package app.knock.client.components.themes

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor

data class InAppFeedNotificationButtonTheme(
    val context: Context,
    val buttonImage: ImageVector = Icons.Default.Notifications,
    val buttonImageForeground: Color = KnockColor.Gray.gray12(context),
    val buttonImageSize: Dp = 24.dp,
    val showBadgeWithCount: Boolean = true,
    val badgeCountTextStyle: TextStyle = TextStyle(color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Medium),
    val badgeColor: Color = KnockColor.Accent.accent9(context),
)