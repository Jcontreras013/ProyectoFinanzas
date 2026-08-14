package com.proyectofinanzas.backend.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/** For append-only / immutable records that only ever record when they were created. */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class CreatedOnlyEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    var id: UUID? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null
}
