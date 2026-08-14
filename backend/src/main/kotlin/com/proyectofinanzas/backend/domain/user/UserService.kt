package com.proyectofinanzas.backend.domain.user

import com.proyectofinanzas.backend.common.ConflictException
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.audit.AuditAction
import com.proyectofinanzas.backend.domain.audit.AuditService
import com.proyectofinanzas.backend.security.SecurityUtils
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditService: AuditService,
) {

    @Transactional(readOnly = true)
    fun list(): List<UserResponse> = userRepository.findAll().map { UserResponse.from(it) }

    @Transactional(readOnly = true)
    fun get(id: UUID): UserResponse =
        UserResponse.from(userRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado") })

    fun create(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmailIgnoreCase(request.email)) {
            throw ConflictException("Ya existe un usuario con ese correo")
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            role = request.role,
        )
        val saved = userRepository.save(user)
        auditService.record(
            "users", requireNotNull(saved.id), AuditAction.CREATE, SecurityUtils.currentUserId(),
            newValue = mapOf("email" to saved.email, "fullName" to saved.fullName, "role" to saved.role),
        )
        return UserResponse.from(saved)
    }

    fun update(id: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado") }
        val before = mapOf("fullName" to user.fullName, "role" to user.role, "active" to user.active)
        user.fullName = request.fullName
        user.role = request.role
        user.active = request.active
        if (!request.password.isNullOrBlank()) {
            user.passwordHash = passwordEncoder.encode(request.password)
        }
        val saved = userRepository.save(user)
        auditService.record(
            "users", id, AuditAction.UPDATE, SecurityUtils.currentUserId(),
            oldValue = before,
            newValue = mapOf("fullName" to saved.fullName, "role" to saved.role, "active" to saved.active),
        )
        return UserResponse.from(saved)
    }

    fun deactivate(id: UUID) {
        val user = userRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado") }
        user.active = false
        userRepository.save(user)
        auditService.record("users", id, AuditAction.DELETE, SecurityUtils.currentUserId(), newValue = null)
    }
}
