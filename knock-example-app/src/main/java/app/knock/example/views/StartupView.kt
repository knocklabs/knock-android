package app.knock.example.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.example.viewmodels.AuthenticationViewModel
import app.knock.example.theme.KnockAndroidTheme

@Composable
fun StartupView(authViewModel: AuthenticationViewModel = viewModel()) {
    val isSignedIn by authViewModel.isSignedIn.collectAsState(initial = false)

    when (isSignedIn) {
        true -> MainView(authViewModel)
        false -> SignInView(authViewModel)
        null -> Text("Loading...")
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewStartupView() {
    KnockAndroidTheme {
        StartupView()
    }
}
