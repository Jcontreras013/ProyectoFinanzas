package com.proyectofinanzas.android.data.remote

import kotlinx.serialization.json.Json
import retrofit2.HttpException

private val json = Json { ignoreUnknownKeys = true }

/** Intenta extraer el mensaje de error legible que envía el backend; si no puede, da un mensaje genérico. */
fun friendlyErrorMessage(throwable: Throwable): String {
    if (throwable is HttpException) {
        val body = throwable.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            runCatching { json.decodeFromString<ApiErrorDto>(body) }.getOrNull()?.let { return it.message }
        }
        return "Error del servidor (código ${throwable.code()})"
    }
    return throwable.message ?: "No se pudo conectar con el servidor"
}
