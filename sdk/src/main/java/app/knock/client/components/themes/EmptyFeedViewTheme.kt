package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor

data class EmptyFeedViewTheme(
    val context: Context,
    val backgroundColor: Color = KnockColor.Surface.surface1(context),
    val title: String? = null,
    val titleTextStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 14.sp, fontWeight = FontWeight.Medium),
    val subtitle: String? = null,
    val subtitleTextStyle: TextStyle = TextStyle(color = KnockColor.Gray.gray12(context), fontSize = 14.sp),
    val icon: ImageVector? = null,
    val iconResId: Int? = null,
    val iconSize: Int? = null,
    val iconColor: Color = KnockColor.Gray.gray12(context),
)