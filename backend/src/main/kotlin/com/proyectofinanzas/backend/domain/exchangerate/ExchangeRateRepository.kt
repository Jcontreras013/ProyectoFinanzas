package com.proyectofinanzas.backend.domain.exchangerate

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface ExchangeRateRepository : JpaRepository<ExchangeRate, UUID> {
    fun findAllByOrderByRateDateDesc(): List<ExchangeRate>
    fun findTopByRateDateLessThanEqualOrderByRateDateDesc(rateDate: LocalDate): Optional<ExchangeRate>
    fun findByRateDate(rateDate: LocalDate): Optional<ExchangeRate>
}
