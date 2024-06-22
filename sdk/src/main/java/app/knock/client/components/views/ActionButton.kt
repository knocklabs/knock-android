package app.knock.client.components.views

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.knock.client.components.KnockColor

data class ActionButtonConfig(
    val backgroundColor: Color = Color.Transparent,
    val borderWidth: Float = 1f,
    val borderColor: Color = Color.Gray,
    val cornerRadius: Float = 4f,
    val textStyle: TextStyle = defaultTextStyle
) {
    companion object {
        val defaultTextStyle = TextStyle(
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

enum class ActionButtonStyle {
    Primary, Secondary;

    fun defaultConfig(context: Context): ActionButtonConfig {
        return when (this) {
            Primary -> ActionButtonConfig(
                backgroundColor = KnockColor.Accent.accent9(context),
                borderWidth = 0f,
                textStyle = ActionButtonConfig.defaultTextStyle.copy(color = Color.White)
            )
            Secondary -> ActionButtonConfig(
                backgroundColor = Color.Transparent,
                borderColor = KnockColor.Gray.gray6(context),
                textStyle = ActionButtonConfig.defaultTextStyle.copy(color = KnockColor.Gray.gray12(context))
            )
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    title: String,
    config: ActionButtonConfig,
    action: () -> Unit
) {
    Button(
        onClick = action,
        colors = ButtonDefaults.buttonColors(containerColor = config.backgroundColor),
        modifier = modifier.padding(0.dp),
        shape = RoundedCornerShape(config.cornerRadius.dp),
        border = BorderStroke(config.borderWidth.dp, config.borderColor),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            modifier = Modifier.padding(0.dp),
            text = title,
            style = config.textStyle,
        )
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewActionButton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(title = "Primary", config = ActionButtonStyle.Primary.defaultConfig(
            LocalContext.current)) {}
        ActionButton(title = "Secondary", config = ActionButtonStyle.Secondary.defaultConfig(
            LocalContext.current)) {}
    }
}