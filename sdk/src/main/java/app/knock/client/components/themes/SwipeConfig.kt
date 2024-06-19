package app.knock.client.components.themes

import android.content.Context

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.knock.client.R
import app.knock.client.components.KnockColor

data class SwipeConfig(
    val action: FeedNotificationRowSwipeAction,
    val title: String?,
    val inverseTitle: String?,
    val titleStyle: TextStyle?,
    val imageId: Int?,
    val inverseImageId: Int?,
    val imageSize: Int?,
    val imageColor: Color?,
    val swipeColor: Color,
)
enum class FeedNotificationRowSwipeAction(
    val defaultTitle: String,
    val defaultInverseTitle: String,
    val defaultImage: Int,
    val defaultInverseImage: Int,

    ) {
    ARCHIVE(
        defaultTitle = "Archive",
        defaultInverseTitle = "Unarchive",
        defaultImage = R.drawable.archive,
        defaultInverseImage = R.drawable.archive
    ),
    MARK_AS_READ(
        defaultTitle = "Read",
        defaultInverseTitle = "Unread",
        defaultImage = R.drawable.mail_open,
        defaultInverseImage = R.drawable.mail_closed
    );

    fun getDefaultSwipeColor(context: Context): Color {
        return when (this) {
            ARCHIVE -> KnockColor.Green.green9(context)
            MARK_AS_READ -> KnockColor.Blue.blue9(context)
        }
    }

    fun getDefaultConfig(context: Context): SwipeConfig {
        return SwipeConfig(
            action = this,
            title = defaultTitle,
            inverseTitle = defaultInverseTitle,
            titleStyle = TextStyle.Default.copy(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            imageColor = Color.White,
            imageId = defaultImage,
            inverseImageId = defaultInverseImage,
            imageSize = 20,
            swipeColor = getDefaultSwipeColor(context),
        )
    }
}