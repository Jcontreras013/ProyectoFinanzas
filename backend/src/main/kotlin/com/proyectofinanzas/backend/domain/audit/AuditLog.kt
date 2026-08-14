package com.proyectofinanzas.backend.domain.audit

import com.proyectofinanzas.backend.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE,
}

@Entity
@Table(name = "audit_log")
class AuditLog(
    @Column(name = "entity_name", nullable = false, length = 100)
    var entityName: String,

    @Column(name = "entity_id", nullable = false)
    var entityId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var action: AuditAction,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value")
    var oldValue: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value")
    var newValue: String? = null,
) {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    var id: UUID? = null
}
