package app.knock.client.components.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.client.R
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.KnockColor
import app.knock.client.components.models.InAppFeedFilter
import app.knock.client.components.themes.InAppFeedViewTheme
import app.knock.client.components.themes.SwipeConfig
import app.knock.client.models.KnockUser
import app.knock.client.models.feed.BlockActionButton
import app.knock.client.models.feed.ButtonSetContentBlock
import app.knock.client.models.feed.FeedItem
import app.knock.client.models.feed.MarkdownContentBlock
import app.knock.client.models.messages.KnockMessageStatusUpdateType
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InAppFeedView(modifier: Modifier = Modifier, viewModel: InAppFeedViewModel, theme: InAppFeedViewTheme = InAppFeedViewTheme(LocalContext.current)) {
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    val filterOptions by viewModel.filterOptions.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val feed by viewModel.feed.collectAsState()
    val showRefreshIndicator by viewModel.showRefreshIndicator.collectAsState()
    val brandingRequired by viewModel.brandingRequired.collectAsState()
    val refreshingFromPull by viewModel.isRefreshingFromPull.collectAsState()

    val pullRefreshState = rememberPullRefreshState(refreshingFromPull, { viewModel.pullToRefresh() })

    fun generateSwipeAction(item: FeedItem, config: SwipeConfig, useInverse: Boolean): SwipeAction {
        return SwipeAction(
            icon = {
                ImageWithText(
                    Modifier.padding(horizontal = 20.dp),
                    image = (if(useInverse) config.inverseImageId else config.imageId)?.let { painterResource(id = it) },
                    contentDescription = if(useInverse) config.inverseTitle else config.title,
                    text = if(useInverse) config.inverseTitle else config.title,
                )
                   },
            background = config.swipeColor,
            onSwipe = { viewModel.didSwipeRow(item, config.action, useInverse) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.upperBackgroundColor)
    ) {
        theme.titleString?.let {
            Text(
                text = it,
                style = theme.titleStyle,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(vertical = 16.dp)
            )
        }

        if (filterOptions.size > 1) {
            FilterTabView(Modifier, viewModel, theme.filterTabTheme)
        }

        if (viewModel.topButtonActions.isNotEmpty()) {
            TopActionButtonsView(viewModel)
            HorizontalDivider(color = KnockColor.Gray.gray4(LocalContext.current), thickness = 1.dp)
        }

        Box(modifier = Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()) {
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
                    EmptyFeedView(InAppFeedFilter.defaultEmptyViewConfig(LocalContext.current, currentFilter.scope))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.background(theme.lowerBackgroundColor)
                    ) {
                        items(feed.entries) { item ->
                            val markAsReadAction: List<SwipeAction> = theme.rowTheme.markAsReadSwipeConfig?.let {
                                listOf(generateSwipeAction(item, it, item.readAt != null))
                            } ?: listOf()

                            val archiveSwipeAction: List<SwipeAction> = theme.rowTheme.archiveSwipeConfig?.let {
                                listOf(generateSwipeAction(item, it, item.archivedAt != null))
                            } ?: listOf()

                            SwipeableActionsBox(
                                Modifier.clickable {
                                    selectedItemId = item.id
                                },
                                startActions = markAsReadAction,
                                endActions = archiveSwipeAction
                            ) {
                                FeedNotificationRow(
                                    Modifier.background(theme.rowTheme.backgroundColor),
                                    item,
                                    theme.rowTheme
                                ) { button ->
                                    viewModel.feedItemButtonTapped(item, button)
                                }
                            }

                        }
                        if (viewModel.isMoreContentAvailable()) {
                            item { LastRowView(theme, viewModel) }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshingFromPull,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (brandingRequired) {
                Image(
                    painter = painterResource(R.drawable.powered_by_knock),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshFeed()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.viewModelScope.launch {
                viewModel.bulkUpdateMessageEngagementStatus(KnockMessageStatusUpdateType.SEEN)
            }
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
            ActionButton(
                Modifier
                    .fillMaxWidth()
                    .weight(1f), action.title, ActionButtonStyle.Secondary.defaultConfig(LocalContext.current), action = {
                viewModel.topActionButtonTapped(action)
            })
        }
    }
}
@Composable
fun LastRowView(theme: InAppFeedViewTheme, viewModel: InAppFeedViewModel) {
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

    LaunchedEffect(Unit) {
        viewModel.fetchNewPageOfFeedItems()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInAppFeedView() {
    val viewModel: InAppFeedViewModel = viewModel(factory = InAppFeedViewModelFactory(LocalContext.current))
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