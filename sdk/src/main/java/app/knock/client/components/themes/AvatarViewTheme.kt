package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor

data class AvatarViewTheme(
    val context: Context,
    val avatarViewBackgroundColor: Color = KnockColor.Gray.gray5(context),
    val avatarViewSize: Int = 32,
    val avatarInitialsTextStyle: TextStyle = TextStyle(
        color = KnockColor.Gray.gray11(context),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
)