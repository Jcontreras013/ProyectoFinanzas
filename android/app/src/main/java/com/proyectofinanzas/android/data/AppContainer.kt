package com.proyectofinanzas.android.data

import android.content.Context
import com.proyectofinanzas.android.data.local.TokenManager
import com.proyectofinanzas.android.data.remote.ApiService
import com.proyectofinanzas.android.data.remote.NetworkModule

/**
 * Contenedor de dependencias manual (sin Hilt): la app es pequeña y esto evita el
 * procesamiento de anotaciones, que añade riesgo/tiempo de build sin aportar mucho a
 * este alcance. Se inicializa una única vez desde la Application.
 */
object AppContainer {
    lateinit var tokenManager: TokenManager
        private set
    lateinit var sessionManager: SessionManager
        private set
    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
        sessionManager = SessionManager(tokenManager)
        apiService = NetworkModule.buildApiService(tokenManager, sessionManager)
    }
}
