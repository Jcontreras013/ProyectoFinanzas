package com.proyectofinanzas.backend.domain.audit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/audit-log")
class AuditLogController(
    private val auditLogRepository: AuditLogRepository,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) entityName: String?,
        pageable: Pageable,
    ): Page<AuditLogResponse> {
        val page = if (entityName != null) {
            auditLogRepository.findAllByEntityNameOrderByOccurredAtDesc(entityName, pageable)
        } else {
            auditLogRepository.findAllByOrderByOccurredAtDesc(pageable)
        }
        return page.map { AuditLogResponse.from(it) }
    }
}
