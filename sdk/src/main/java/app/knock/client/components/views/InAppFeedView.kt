package app.knock.client.components.views

import FeedNotificationRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.client.R
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.KnockColor
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.components.themes.InAppFeedTheme
import app.knock.client.models.KnockUser
import app.knock.client.models.feed.BlockActionButton
import app.knock.client.models.feed.ButtonSetContentBlock
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.MarkdownContentBlock
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import java.time.ZonedDateTime

@Composable
fun InAppFeedView(viewModel: InAppFeedViewModel, theme: InAppFeedTheme = InAppFeedTheme(LocalContext.current)) {
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    val filterOptions by viewModel.filterOptions.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val feed by viewModel.feed.collectAsState()
    val showRefreshIndicator by viewModel.showRefreshIndicator.collectAsState()
    val brandingRequired by viewModel.brandingRequired.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.upperBackgroundColor)
    ) {
        theme.titleString?.let {
            Text(
                text = it,
                style = theme.titleStyle,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }

        if (filterOptions.size > 1) {
            FilterTabView(viewModel)
        }

        if (viewModel.topButtonActions.isNotEmpty()) {
            TopActionButtonsView(viewModel)
            HorizontalDivider(color = KnockColor.Gray.gray4(LocalContext.current), thickness = 1.dp)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                showRefreshIndicator -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                feed.entries.isEmpty() -> {
                    EmptyFeedView(InAppFeedFilter.defaultEmptyViewConfig(LocalContext.current, currentFilter.scope)) {
                        viewModel.refreshFeed()
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.background(theme.lowerBackgroundColor)
                    ) {
                        items(feed.entries) { item ->
                            FeedNotificationRow(item, theme.rowTheme) { buttonTapString ->
                                selectedItemId = item.id
                                viewModel.feedItemButtonTapped(item, buttonTapString)
                            }
//                            .background(
//                                if (selectedItemId == item.id) Color.Gray.copy(alpha = 0.4f) else Color.Transparent
//                            )
                        }
                        if (viewModel.isMoreContentAvailable()) {
                            item { LastRowView(theme) }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }

            if (brandingRequired) {
                Image(
                    painter = painterResource(R.drawable.powered_by_knock),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshFeed()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.bulkUpdateMessageEngagementStatus(KnockMessageStatusUpdateType.SEEN)
        }
    }
}

@Composable
fun TopActionButtonsView(viewModel: InAppFeedViewModel) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        viewModel.topButtonActions.forEach { action ->
            ActionButton(Modifier.fillMaxWidth().weight(1f), action.title, ActionButtonStyle.Secondary.defaultConfig(LocalContext.current), action = {
                viewModel.topActionButtonTapped(action)
            })
        }
    }
}
@Composable
fun LastRowView(theme: InAppFeedTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(theme.rowTheme.backgroundColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInAppFeedView() {
    val viewModel: InAppFeedViewModel = viewModel(factory = InAppFeedViewModelFactory())
    val markdown1 = MarkdownContentBlock(name = "markdown", content = "", rendered = "<p>Hey <strong>Dennis</strong> 👋 - Ian Malcolm completed an activity.</p>")
    val item = FeedItem(
        id = "1",
        feedCursor = "test",
        actors = listOf(KnockUser("fake id", "John Doe", email = null, phoneNumber = null, avatar = "https://via.placeholder.com/150")),
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
    viewModel.feed.value.entries = listOf(item, item, item, item, item)
    InAppFeedView(viewModel = viewModel)
}