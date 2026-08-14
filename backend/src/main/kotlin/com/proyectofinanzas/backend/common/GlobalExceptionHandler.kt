package com.proyectofinanzas.backend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String>? = null,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
        respond(HttpStatus.NOT_FOUND, ex.message ?: "Recurso no encontrado")

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRule(ex: BusinessRuleException): ResponseEntity<ApiError> =
        respond(HttpStatus.UNPROCESSABLE_ENTITY, ex.message ?: "Regla de negocio violada")

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ApiError> =
        respond(HttpStatus.CONFLICT, ex.message ?: "Conflicto de estado")

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ApiError> =
        respond(HttpStatus.UNAUTHORIZED, "Credenciales inválidas")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiError> =
        respond(HttpStatus.FORBIDDEN, "No tiene permisos para esta acción")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "inválido")
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "Error de validación",
                fieldErrors = fieldErrors,
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiError> =
        respond(HttpStatus.BAD_REQUEST, ex.message ?: "Solicitud inválida")

    private fun respond(status: HttpStatus, message: String): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(
            ApiError(status = status.value(), error = status.reasonPhrase, message = message)
        )
}
