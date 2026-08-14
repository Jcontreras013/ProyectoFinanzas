package com.proyectofinanzas.backend.domain.audit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AuditLogRepository : JpaRepository<AuditLog, UUID> {
    fun findAllByOrderByOccurredAtDesc(pageable: Pageable): Page<AuditLog>
    fun findAllByEntityNameOrderByOccurredAtDesc(entityName: String, pageable: Pageable): Page<AuditLog>
}
