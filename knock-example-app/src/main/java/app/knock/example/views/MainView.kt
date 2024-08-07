package app.knock.example.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.client.Knock
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.themes.InAppFeedViewTheme
import app.knock.client.components.views.InAppFeedNotificationButton
import app.knock.client.components.views.InAppFeedView
import app.knock.client.modules.FeedManager
import app.knock.example.Utils
import app.knock.example.viewmodels.AuthenticationViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(authViewModel: AuthenticationViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val feedViewModel: InAppFeedViewModel = viewModel(factory = InAppFeedViewModelFactory(LocalContext.current))
    var showingSheet by remember { mutableStateOf(false) }
    val feed by feedViewModel.feed.collectAsState()
    val theme = InAppFeedViewTheme(context = LocalContext.current, titleString = null)

    LaunchedEffect(key1 = Unit) {
        if (Knock.shared.feedManager == null) {
            Knock.shared.feedManager = FeedManager(feedId = Utils.inAppChannelId)
            feedViewModel.connectFeedAndObserveNewMessages()
        }

        feedViewModel.didTapFeedItemRowPublisher
            .onEach { feedItem ->
                // Handle the feed item row tap event
            }
            .launchIn(this)

        feedViewModel.didTapFeedItemButtonPublisher
            .onEach { feedItemButtonEvent ->
                // Handle the feed item button block tap event
            }
            .launchIn(this)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(selectedTab == 0) "Messages" else "Preferences") },
                actions = {
                    InAppFeedNotificationButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        unreadCount = feed.meta.unreadCount
                    ) {
                        showingSheet = true
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Messages")
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Preferences")
                    }
                }
            }
        }
    ) { _ ->
        when (selectedTab) {
            0 -> MessageComposeView()
            1 -> PreferencesView(authViewModel)
        }
    }

    if (showingSheet) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = {
                    Text("Notifications",
                        style = theme.titleStyle,
                    )
                        },
                actions = {
                    IconButton(
                        onClick = { showingSheet = false },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.upperBackgroundColor
                )
            )
        }) { innerPadding ->
            InAppFeedView(Modifier.padding(innerPadding), feedViewModel, theme = theme)
        }
    }
}

@Preview()
@Composable
private fun PreviewMainView() {
    MainView(AuthenticationViewModel())
}