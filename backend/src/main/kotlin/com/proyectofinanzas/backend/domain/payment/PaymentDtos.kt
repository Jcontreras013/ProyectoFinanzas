package com.proyectofinanzas.backend.domain.payment

import com.proyectofinanzas.backend.common.Currency
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class CreatePaymentRequest(
    val invoiceId: UUID? = null,
    val expenseId: UUID? = null,
    @field:NotNull @field:DecimalMin(value = "0.0001") val amount: BigDecimal,
    @field:NotNull val currency: Currency,
    val exchangeRate: BigDecimal? = null,
    @field:NotNull val paymentDate: LocalDate,
    @field:NotNull val method: PaymentMethod,
)

data class PaymentResponse(
    val id: UUID,
    val paymentNumber: Long,
    val invoiceId: UUID?,
    val expenseId: UUID?,
    val amount: BigDecimal,
    val currency: Currency,
    val exchangeRate: BigDecimal,
    val amountInBase: BigDecimal,
    val paymentDate: LocalDate,
    val method: PaymentMethod,
    val journalEntryId: UUID?,
    val createdAt: Instant,
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            id = requireNotNull(payment.id),
            paymentNumber = payment.paymentNumber,
            invoiceId = payment.invoice?.id,
            expenseId = payment.expense?.id,
            amount = payment.amount,
            currency = payment.currency,
            exchangeRate = payment.exchangeRate,
            amountInBase = payment.amountInBase,
            paymentDate = payment.paymentDate,
            method = payment.method,
            journalEntryId = payment.journalEntry?.id,
            createdAt = requireNotNull(payment.createdAt),
        )
    }
}
