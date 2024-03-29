package app.knock.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.knock.client.Knock
import app.knock.client.modules.isAuthenticated
import app.knock.client.modules.signIn
import app.knock.client.modules.signOut
import app.knock.example.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val _isSignedIn = MutableStateFlow<Boolean?>(null)
    val isSignedIn = _isSignedIn.asStateFlow()

    init {
        signIn(Utils.userId)
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val isAuthenticated = Knock.shared.isAuthenticated()
            _isSignedIn.value = isAuthenticated
        }
    }

    fun signIn(userId: String) {
        viewModelScope.launch {
            Knock.shared.signIn(userId = userId, userToken = null)
            _isSignedIn.value = true
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Knock.shared.signOut()
            _isSignedIn.value = false
        }
    }
}