package app.knock.client.components.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.knock.client.components.KnockColor
import app.knock.client.components.themes.FeedNotificationRowTheme
import app.knock.client.models.KnockUser
import app.knock.client.models.feed.BlockActionButton
import app.knock.client.models.feed.ButtonSetContentBlock
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.MarkdownContentBlock
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun FeedNotificationRow(
    modifier: Modifier = Modifier,
    item: FeedItem,
    theme: FeedNotificationRowTheme = FeedNotificationRowTheme(LocalContext.current),
    buttonTapAction: (BlockActionButton) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                if (item.readAt == null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(theme.unreadNotificationCircleColor, CircleShape)
                    )
                }
                if (theme.showAvatarView && item.actors.isNotEmpty()) {
                    AvatarView(item.actors.firstOrNull()?.avatar, item.actors.firstOrNull()?.name)
                }
            }

            Column(horizontalAlignment = Alignment.Start) {
                item.blocks.forEach { block ->
                    when (block) {
                        is MarkdownContentBlock -> MarkdownContent(block)
                        is ButtonSetContentBlock -> ActionButtonsContent(block, theme, buttonTapAction)
                        else -> {}
                    }
                }
                item.insertedAt?.let {
                    val localDateTime = it.withZoneSameInstant(ZoneId.systemDefault())
                    Text(
                        text = theme.sentAtDateFormatter.format(localDateTime),
                        style = theme.sentAtDateTextStyle
                    )
                }
            }
        }
        HorizontalDivider(color = KnockColor.Gray.gray4(LocalContext.current), thickness = 1.dp)
    }
}

@Composable
fun MarkdownContent(block: MarkdownContentBlock) {
    MarkdownContentView(block.rendered)
}

@Composable
fun ActionButtonsContent(
    block: ButtonSetContentBlock,
    theme: FeedNotificationRowTheme,
    buttonTapAction: (BlockActionButton) -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        block.buttons.forEach { button ->
            val config = if (button.name == "primary") theme.primaryActionButtonConfig else theme.secondaryActionButtonConfig
            ActionButton(title = button.label, config = config) {
                buttonTapAction(button)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNotificationRow() {
    val markdown1 = MarkdownContentBlock(name = "markdown", content = "", rendered = "<p>Hey <strong>Dennis</strong> 👋 - Ian Malcolm completed an activity.</p>")
    val markdown2 = MarkdownContentBlock(name = "markdown", content = "", rendered = "<p>Here's a new notification from <strong>Eleanor Price</strong>:</p><blockquote><p>test message test message test message test message test message test message test message test message test message </p></blockquote>")
    val item = FeedItem(
        id = "1",
        feedCursor = "test",
        actors = listOf(KnockUser("fake id", "John Doe", email = null, phoneNumber = null, avatar = null)),
        blocks = listOf(
            markdown1,
            ButtonSetContentBlock(
                name = "buttons",
                buttons = listOf(
                    BlockActionButton("Primary", "primary", ""),
                    BlockActionButton("Secondary", "secondary", "")
                )
            )
        ),
        readAt = null,
        insertedAt = ZonedDateTime.now(),
        totalActivities = 2,
        totalActors = 1
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                actions = {}
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)) {
            LazyColumn {
                items(listOf(item)) {
                    FeedNotificationRow(item = item, buttonTapAction = {})
                }
            }
        }
    }
}