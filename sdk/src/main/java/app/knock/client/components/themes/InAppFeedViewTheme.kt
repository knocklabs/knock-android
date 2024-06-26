package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
data class InAppFeedViewTheme(
    val context: Context,
    val rowTheme: FeedNotificationRowTheme = FeedNotificationRowTheme(context), // Defines the UI customization of the row items.
    val filterTabTheme: FilterTabTheme = FilterTabTheme(context), // Defines the UI customization of the top filter tabs.
    val titleString: String? = "Notifications", // Sets the title of the view. If set to nil, then the title view will be hidden entirely. This is usefor if you want to have a completely custom title view.
    val titleStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 36.sp, fontWeight = FontWeight.Bold), // Sets the textStyle for the title of the view.
    val upperBackgroundColor: Color = KnockColor.Surface.surface1(context), // Sets the background color of the top portion of the view (title view, filter view, and top action buttons view).
    val lowerBackgroundColor: Color = KnockColor.Surface.surface1(context) // Sets the background color of the bottom portion of the view (the list).
)