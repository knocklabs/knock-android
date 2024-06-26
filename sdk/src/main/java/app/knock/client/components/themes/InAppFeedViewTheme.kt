package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
data class InAppFeedViewTheme(
    val context: Context,
    val rowTheme: FeedNotificationRowTheme = FeedNotificationRowTheme(context),
    val filterTabTheme: FilterTabTheme = FilterTabTheme(context),
    val titleString: String? = "Notifications",
    val titleStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 36.sp, fontWeight = FontWeight.Bold),
    val upperBackgroundColor: Color = KnockColor.Surface.surface1(context),
    val lowerBackgroundColor: Color = KnockColor.Surface.surface1(context)
)