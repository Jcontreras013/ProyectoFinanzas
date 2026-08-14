package com.proyectofinanzas.backend.domain.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class LoginResponse(
    val token: String,
    val user: UserResponse,
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val fullName: String,
    val role: Role,
    val active: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = requireNotNull(user.id),
            email = user.email,
            fullName = user.fullName,
            role = user.role,
            active = user.active,
            createdAt = requireNotNull(user.createdAt),
        )
    }
}

data class CreateUserRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") val password: String,
    @field:NotBlank val fullName: String,
    val role: Role,
)

data class UpdateUserRequest(
    @field:NotBlank val fullName: String,
    val role: Role,
    val active: Boolean,
    @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") val password: String? = null,
)
