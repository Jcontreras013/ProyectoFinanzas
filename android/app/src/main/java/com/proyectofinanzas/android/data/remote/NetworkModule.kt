package com.proyectofinanzas.android.data.remote

import com.proyectofinanzas.android.BuildConfig
import com.proyectofinanzas.android.data.SessionManager
import com.proyectofinanzas.android.data.local.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    fun buildApiService(tokenManager: TokenManager, sessionManager: SessionManager): ApiService {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val authInterceptor = Interceptor { chain ->
            val token = tokenManager.tokenSync()
            val request = if (token != null) {
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            val response = chain.proceed(request)
            if (response.code == 401) {
                sessionManager.forceLogout()
            }
            response
        }

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
