package com.proyectofinanzas.backend.domain.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.UUID

interface PaymentRepository : JpaRepository<Payment, UUID> {

    @Query("select coalesce(sum(p.amountInBase), 0) from Payment p where p.invoice.id = :invoiceId")
    fun sumAmountInBaseByInvoiceId(@Param("invoiceId") invoiceId: UUID): BigDecimal

    @Query("select coalesce(sum(p.amountInBase), 0) from Payment p where p.expense.id = :expenseId")
    fun sumAmountInBaseByExpenseId(@Param("expenseId") expenseId: UUID): BigDecimal

    fun findByInvoiceIdOrderByPaymentDateDesc(invoiceId: UUID): List<Payment>
    fun findByExpenseIdOrderByPaymentDateDesc(expenseId: UUID): List<Payment>
}
