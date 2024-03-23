package app.knock.example.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.client.models.preferences.ChannelTypePreferenceItem
import app.knock.example.viewmodels.AuthenticationViewModel
import app.knock.example.viewmodels.PreferencesViewModel
import arrow.core.Either

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesView(authViewModel: AuthenticationViewModel) {
    val preferencesViewModel: PreferencesViewModel = viewModel()
    val preferenceSet by preferencesViewModel.preferenceSet.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(),
        ) {
            preferenceSet?.channelTypes?.let { types ->
                types.asArrayOfPreferenceItems().forEach { item ->
                    PreferenceToggleRow(item) {
                        preferencesViewModel.updatePreference(item.id, Either.Left(it))
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    authViewModel.signOut()
                }) {
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
fun PreferenceToggleRow(
    item: ChannelTypePreferenceItem,
    onPreferenceChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = item.id.name)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = (item.value as? Either.Left<Boolean>)?.value ?: false,
            onCheckedChange = onPreferenceChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}


@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun PreferencesViewPreviewView() {
    PreferencesView(AuthenticationViewModel())
}