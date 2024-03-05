package com.example.knock_example_app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.knock.client.KnockComponentActivity
import com.example.knock_example_app.ui.theme.KnockandroidTheme
import com.example.knock_example_app.views.StartupView


class MainActivity : KnockComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KnockandroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartupView()
                }
            }
        }
    }
}
