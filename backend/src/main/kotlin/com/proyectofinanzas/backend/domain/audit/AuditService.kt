package com.proyectofinanzas.backend.domain.audit

import com.fasterxml.jackson.databind.ObjectMapper
import com.proyectofinanzas.backend.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) {
    /** Se ejecuta en la misma transacción del cambio que audita: si esta falla, el cambio se revierte también. */
    @Transactional(propagation = Propagation.MANDATORY)
    fun record(entityName: String, entityId: UUID, action: AuditAction, userId: UUID, newValue: Any?, oldValue: Any? = null) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("Usuario no encontrado al registrar auditoría: $userId")
        }
        val log = AuditLog(
            entityName = entityName,
            entityId = entityId,
            action = action,
            user = user,
            oldValue = oldValue?.let { objectMapper.writeValueAsString(it) },
            newValue = newValue?.let { objectMapper.writeValueAsString(it) },
        )
        auditLogRepository.save(log)
    }
}
