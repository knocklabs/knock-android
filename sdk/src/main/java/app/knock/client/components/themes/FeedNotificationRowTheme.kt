package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor
import app.knock.client.components.views.ActionButtonConfig
import app.knock.client.components.views.ActionButtonStyle
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FeedNotificationRowTheme(
    val context: Context,
    val backgroundColor: Color = KnockColor.Surface.surface1(context), // Background color of the FeedNotificationRow
    val notificationContentCSS: String? = null, // Customize the css of the markdown html of the notification body
    val unreadNotificationCircleColor: Color = KnockColor.Blue.blue9(context), // Color of the unread circle indicator in the top left of the row
    val showAvatarView: Boolean = true, // Show or hide the avatar/initials view in the upper left corner of the row
    val primaryActionButtonConfig: ActionButtonConfig = ActionButtonStyle.Primary.defaultConfig(context), // Styling for primary action buttons
    val secondaryActionButtonConfig: ActionButtonConfig = ActionButtonStyle.Secondary.defaultConfig(context), // Styling for secondary action buttons
    val sentAtDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a", Locale.getDefault()),
    val sentAtDateTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Gray),
    val markAsReadSwipeConfig: SwipeConfig? = FeedNotificationRowSwipeAction.MARK_AS_READ.getDefaultConfig(context), // Set this to null to remove the left swipe action
    val archiveSwipeConfig: SwipeConfig? = FeedNotificationRowSwipeAction.ARCHIVE.getDefaultConfig(context) // Set this to null to remove the right swipe action
)