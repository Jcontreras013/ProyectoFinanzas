package com.proyectofinanzas.backend.domain.party

import com.proyectofinanzas.backend.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

enum class PartyType {
    CUSTOMER,
    VENDOR,
    BOTH,
}

@Entity
@Table(name = "parties")
class Party(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: PartyType,

    @Column(nullable = false)
    var name: String,

    @Column(length = 20)
    var rtn: String? = null,

    var email: String? = null,

    var phone: String? = null,

    var address: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : BaseEntity()
