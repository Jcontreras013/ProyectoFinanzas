package com.proyectofinanzas.backend.domain.exchangerate

import com.proyectofinanzas.backend.common.BusinessRuleException
import com.proyectofinanzas.backend.common.ConflictException
import com.proyectofinanzas.backend.domain.user.UserRepository
import com.proyectofinanzas.backend.security.SecurityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class ExchangeRateService(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun list(): List<ExchangeRateResponse> =
        exchangeRateRepository.findAllByOrderByRateDateDesc().map { ExchangeRateResponse.from(it) }

    fun upsert(request: ExchangeRateRequest): ExchangeRateResponse {
        val existing = exchangeRateRepository.findByRateDate(request.rateDate)
        if (existing.isPresent) {
            val entity = existing.get()
            entity.rate = request.rate
            return ExchangeRateResponse.from(exchangeRateRepository.save(entity))
        }
        val createdBy = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow { ConflictException("Usuario no encontrado") }
        val entity = ExchangeRate(rateDate = request.rateDate, rate = request.rate, createdBy = createdBy)
        return ExchangeRateResponse.from(exchangeRateRepository.save(entity))
    }

    /** Tasa vigente para una fecha: la más reciente registrada en o antes de esa fecha. */
    @Transactional(readOnly = true)
    fun rateFor(date: LocalDate): BigDecimal =
        exchangeRateRepository.findTopByRateDateLessThanEqualOrderByRateDateDesc(date)
            .map { it.rate }
            .orElseThrow {
                BusinessRuleException(
                    "No hay tasa de cambio registrada para $date ni antes; registre una en Tasas de cambio"
                )
            }
}
