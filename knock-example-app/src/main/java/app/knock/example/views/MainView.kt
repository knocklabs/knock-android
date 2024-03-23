package app.knock.example.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.example.viewmodels.AuthenticationViewModel
import app.knock.example.viewmodels.InAppFeedViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(authViewModel: AuthenticationViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val feedViewModel: InAppFeedViewModel = viewModel()
    var showingSheet by remember { mutableStateOf(false) }
    val feedState by feedViewModel.feed.collectAsState()

    LaunchedEffect(key1 = Unit) {
        feedViewModel.initializeFeed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(selectedTab == 0) "Messages" else "Preferences") },
                actions = {
                    val unseenCount = feedState?.meta?.unseenCount ?: 0

                    IconButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = { showingSheet = true }) {
                        if (unseenCount > 0) {
                            NotificationIconWithBadge(unseenCount)
                        } else {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
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
        InAppFeedView(feedViewModel) {
            showingSheet = false
        }
    }
}

@Composable
fun NotificationIconWithBadge(unseenCount: Int) {
    BadgedBox(badge = {
        if (unseenCount > 0) {
            // Display the badge with the unseen count
            Badge { Text("$unseenCount") }
        }
    }) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications"
        )
    }
}

@Preview()
@Composable
private fun PreviewMainView() {
    MainView(AuthenticationViewModel())
}