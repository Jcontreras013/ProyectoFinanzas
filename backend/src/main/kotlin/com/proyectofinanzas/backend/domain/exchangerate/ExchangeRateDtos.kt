package com.proyectofinanzas.backend.domain.exchangerate

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ExchangeRateRequest(
    @field:NotNull val rateDate: LocalDate,
    @field:NotNull @field:DecimalMin(value = "0.000001") val rate: BigDecimal,
)

data class ExchangeRateResponse(
    val id: UUID,
    val rateDate: LocalDate,
    val rate: BigDecimal,
    val createdByName: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(entity: ExchangeRate) = ExchangeRateResponse(
            id = requireNotNull(entity.id),
            rateDate = entity.rateDate,
            rate = entity.rate,
            createdByName = entity.createdBy.fullName,
            createdAt = requireNotNull(entity.createdAt),
        )
    }
}
