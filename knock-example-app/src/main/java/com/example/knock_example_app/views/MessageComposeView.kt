package com.example.knock_example_app.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.knock_example_app.ui.theme.KnockandroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageComposeView(showingSheet: Boolean) {
    var message by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Send an in-app notification") })
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize()
            ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Message:")
            Spacer(modifier = Modifier.height(4.dp))
            TextField(value = message, onValueChange = { message = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    // Call your send notification logic here
                    message = "" // Clear the message after sending
                }) {
                    Text("Send notification")
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewMessageComposeView() {
    KnockandroidTheme {
        MessageComposeView(false)
    }
}