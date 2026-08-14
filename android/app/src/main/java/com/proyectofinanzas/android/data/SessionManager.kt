package com.proyectofinanzas.android.data

import com.proyectofinanzas.android.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Estado de sesión compartido por toda la app: null = aún no se sabe, true/false = resuelto. */
class SessionManager(private val tokenManager: TokenManager) {

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    suspend fun checkInitialAuth() {
        _isAuthenticated.value = tokenManager.tokenFlow.first() != null
    }

    suspend fun login(token: String, userName: String) {
        tokenManager.saveToken(token)
        _currentUserName.value = userName
        _isAuthenticated.value = true
    }

    suspend fun logout() {
        tokenManager.clearToken()
        _currentUserName.value = null
        _isAuthenticated.value = false
    }

    /** Llamado desde el interceptor de red (hilo no-suspend) cuando el backend responde 401. */
    fun forceLogout() {
        runBlocking { tokenManager.clearToken() }
        _currentUserName.value = null
        _isAuthenticated.value = false
    }
}
