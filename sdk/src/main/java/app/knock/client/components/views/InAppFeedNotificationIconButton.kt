package app.knock.client.components.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.knock.client.components.themes.InAppFeedNotificationIconButtonTheme

@Composable
fun InAppFeedNotificationIconButton(
    modifier: Modifier = Modifier,
    count: Int,
    theme: InAppFeedNotificationIconButtonTheme = InAppFeedNotificationIconButtonTheme(),
    action: () -> Unit,
) {
    val countText = if (theme.showBadgeWithCount && count > 0) {
        if (count > 99) "99" else "$count"
    } else {
        ""
    }

//    val badgePadding = if (countText.length > 1) 4.dp else 6.dp

//    Box(modifier = modifier) {
//        IconButton(modifier = Modifier.padding(0.dp), onClick = action) {
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
//            IconButton(modifier = Modifier.padding(0.dp), onClick = action) {
//                Icon(
//                    imageVector = Icons.Filled.Notifications,
//                    contentDescription = null,
//                    modifier = Modifier.size(theme.buttonImageSize).padding(0.dp)
//                )
//
//            if (count > 0) {
//                CustomBadge(
//                    count = count,
//                    modifier = Modifier
////                    .offset(x = theme.buttonImageSize - 8.dp, y = (-8).dp)
//                )
//            }
//        }
//    }

    Box(Modifier.padding(24.dp)) {
        IconButton(onClick = action) {
//        Row {
//            Icon(
//                imageVector = Icons.Filled.Notifications,
//                contentDescription = "Notifications"
//            )
//
//            Box(modifier = Modifier.clip(CircleShape).background(Color.Red).padding(4.dp)) {
//                Text(countText, style = theme.badgeCountTextStyle)
//            }
//        }
            BadgedBox(badge = {
                if (count > 0) {
                    Badge {
                        Text(countText, style = theme.badgeCountTextStyle)
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications"
                )
            }
        }
    }

}

@Suppress("unused")
@Composable
fun CustomBadge(
    modifier: Modifier,
    count: Int,
    textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 10.sp),
    backgroundColor: Color = Color.Red
) {
    val countText = if (count > 99) "99+" else "$count"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = countText, style = textStyle)
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewInAppFeedNotificationIconButton() {
    MaterialTheme {
        InAppFeedNotificationIconButton(
            count = 25,
            action = {}
        )
    }
}