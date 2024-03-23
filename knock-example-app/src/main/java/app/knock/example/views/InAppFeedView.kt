package app.knock.example.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.knock.client.models.feed.FeedItem
import app.knock.example.viewmodels.InAppFeedViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppFeedView(inAppFeedViewModel: InAppFeedViewModel, onDismiss: () -> Unit) {
    val feed by inAppFeedViewModel.feed.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                actions = {
                    IconButton(onClick = {
                        inAppFeedViewModel.markAllAsSeen()
                        onDismiss()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
             feed?.entries?.let { feedItems ->
                 if (feedItems.isEmpty()) {
                     Text("No notifications yet")
                     Text("We'll let you know when we've got something new for you.")
                 } else {
                     LazyColumn {
                         items(
                             items = feedItems,
                             key = { item -> item.id }
                         ) { item ->
                             NotificationRow(item = item) {
                                 inAppFeedViewModel.archiveItem(it)
                             }
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

    Column(verticalArrangement = Arrangement.Center) {

        Row(Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (item.seenAt == null) {
                Box(modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Blue)
                    .align(Alignment.CenterVertically))
            }

            MarkdownText(
                markdown = markdown,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp).align(Alignment.CenterVertically)
            )

            IconButton(modifier = Modifier
                .size(24.dp),
                onClick = { onArchive(item) }
            ) {
                Icon(modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Archive",
                    tint = Color(0xFFFF7F7F))
            }
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun InAppFeedViewView() {
    InAppFeedView(InAppFeedViewModel()) { }
}
