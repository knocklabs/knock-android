package app.knock.client.components

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import app.knock.client.R

@Suppress("unused")
object KnockColor {
    object Gray {
        fun gray3(context: Context) = getColorFromResource(context, R.color.gray3)
        fun gray4(context: Context) = getColorFromResource(context, R.color.gray4)
        fun gray5(context: Context) = getColorFromResource(context, R.color.gray5)
        fun gray6(context: Context) = getColorFromResource(context, R.color.gray6)
        fun gray9(context: Context) = getColorFromResource(context, R.color.gray9)
        fun gray11(context: Context) = getColorFromResource(context, R.color.gray11)
        fun gray12(context: Context) = getColorFromResource(context, R.color.gray12)
    }

    object Accent {
        fun accent3(context: Context) = getColorFromResource(context, R.color.accent3)
        fun accent9(context: Context) = getColorFromResource(context, R.color.accent9)
        fun accent11(context: Context) = getColorFromResource(context, R.color.accent11)
    }

    object Surface {
        fun surface1(context: Context) = getColorFromResource(context, R.color.surface1)
    }

    object Blue {
        fun blue9(context: Context) = getColorFromResource(context, R.color.blue9)
    }

    object Green {
        fun green9(context: Context) = getColorFromResource(context, R.color.green9)
    }

    private fun getColorFromResource(context: Context, resId: Int): Color {
        return Color(ContextCompat.getColor(context, resId))
    }
}