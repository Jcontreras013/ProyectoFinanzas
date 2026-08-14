package com.proyectofinanzas.backend.domain.audit

import java.time.Instant
import java.util.UUID

data class AuditLogResponse(
    val id: UUID,
    val entityName: String,
    val entityId: UUID,
    val action: AuditAction,
    val userName: String,
    val occurredAt: Instant,
    val oldValue: String?,
    val newValue: String?,
) {
    companion object {
        fun from(log: AuditLog) = AuditLogResponse(
            id = requireNotNull(log.id),
            entityName = log.entityName,
            entityId = log.entityId,
            action = log.action,
            userName = log.user.fullName,
            occurredAt = log.occurredAt,
            oldValue = log.oldValue,
            newValue = log.newValue,
        )
    }
}
