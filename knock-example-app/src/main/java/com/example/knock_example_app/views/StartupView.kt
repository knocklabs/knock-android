package com.example.knock_example_app.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.knock_example_app.viewmodels.AuthenticationViewModel
import com.example.knock_example_app.ui.theme.KnockandroidTheme

@Composable
fun StartupView(authViewModel: AuthenticationViewModel = viewModel()) {
    val isSignedIn by authViewModel.isSignedIn.collectAsState(initial = false)

//    LaunchedEffect(key1 = Unit) {
//        authViewModel.signIn(userId = Utils.userId)
//    }
    when (isSignedIn) {
        true -> MainView()
        false -> SignInView(authViewModel)
        null -> Text("Loading...")
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewStartupView() {
    KnockandroidTheme {
        StartupView()
    }
}
