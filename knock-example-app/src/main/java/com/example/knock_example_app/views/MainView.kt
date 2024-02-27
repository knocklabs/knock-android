package com.example.knock_example_app.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.knock_example_app.ui.theme.KnockandroidTheme
import com.example.knock_example_app.viewmodels.InAppFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val feedViewModel: InAppFeedViewModel = viewModel()
    var showingSheet by remember { mutableStateOf(false) }

    // Example implementation of LaunchedEffect for feed initialization
    LaunchedEffect(key1 = Unit) {
        feedViewModel.initializeFeed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Name") },
                actions = {
                    IconButton(onClick = { showingSheet = true }) {
                        if ((feedViewModel.feed.value?.meta?.unseenCount ?: 0) > 0) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Notifications")
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
    ) { paddingValues ->
        when (selectedTab) {
            0 -> MessageComposeView(showingSheet = false)
            1 -> Text("Preferences", Modifier.padding(paddingValues))
        }
    }

    if (showingSheet) {
        InAppFeedView(feedViewModel) {
            showingSheet = false
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun PreviewMainView() {
    KnockandroidTheme {
        MainView()
    }
}