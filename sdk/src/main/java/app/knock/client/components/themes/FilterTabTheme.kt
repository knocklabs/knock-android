package app.knock.client.components.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor

data class FilterTabTheme(
    val context: Context,
    val selectedColor: Color = KnockColor.Accent.accent11(context),
    val unselectedColor: Color = KnockColor.Gray.gray11(context),
    val textStyle: TextStyle = TextStyle(
        color = KnockColor.Accent.accent11(context),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
)

