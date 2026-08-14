package com.proyectofinanzas.android.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.LoginRequest
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("admin@demo.com")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(value: String) {
        email = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun login() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = AppContainer.apiService.login(LoginRequest(email, password))
                AppContainer.sessionManager.login(response.token, response.user.fullName)
            } catch (e: Exception) {
                error = friendlyErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }
}
