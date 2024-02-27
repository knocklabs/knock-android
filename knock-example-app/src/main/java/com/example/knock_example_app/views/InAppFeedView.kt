package com.example.knock_example_app.views

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.knock.client.models.feed.FeedItem
import com.example.knock_example_app.ui.theme.KnockandroidTheme
import com.example.knock_example_app.viewmodels.InAppFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppFeedView(inAppFeedViewModel: InAppFeedViewModel, onDismiss: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                actions = {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            if (inAppFeedViewModel.feedEntries().isEmpty()) {
                Text("No notifications yet")
                Text("We'll let you know when we've got something new for you.")
            } else {
                LazyColumn {
                    items(
                        items = inAppFeedViewModel.feedEntries(), // Your list of FeedItem objects
                        key = { item -> item.id } // Provide a unique key for each item
                    ) { item ->
                        // How each item should be rendered
                        NotificationRow(item = item) {
                            inAppFeedViewModel.archiveItem(it)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun NotificationRow(item: FeedItem, onArchive: (item: FeedItem) -> Unit) {
    val markdown = item.blocks.firstOrNull { it.name == "body" }?.rendered ?: ""
    val htmlContent = "<span style=\"font-family: sans-serif; font-size: 20px;\">$markdown</span>"

    Row {
        if (item.seenAt == null) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Unseen", tint = Color.Blue)
        }
        HtmlText(html = htmlContent)

        IconButton(onClick = { onArchive(item) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Archive")
        }
    }
}

@Composable
fun HtmlText(html: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    })
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun InAppFeedViewView() {
    KnockandroidTheme {
        InAppFeedView(InAppFeedViewModel()) { }
    }
}
