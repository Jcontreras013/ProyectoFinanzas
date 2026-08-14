package com.proyectofinanzas.backend.domain.exchangerate

import com.proyectofinanzas.backend.common.CreatedOnlyEntity
import com.proyectofinanzas.backend.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

/** Tasa HNL por 1 USD, mantenida manualmente. */
@Entity
@Table(name = "exchange_rates")
class ExchangeRate(
    @Column(name = "rate_date", nullable = false, unique = true)
    var rateDate: LocalDate,

    @Column(nullable = false, precision = 12, scale = 6)
    var rate: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User,
) : CreatedOnlyEntity()
