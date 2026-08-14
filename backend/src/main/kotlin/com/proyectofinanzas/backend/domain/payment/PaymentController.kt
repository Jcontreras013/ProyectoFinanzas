package com.proyectofinanzas.backend.domain.payment

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun create(@Valid @RequestBody request: CreatePaymentRequest): PaymentResponse = paymentService.create(request)

    @GetMapping
    fun list(
        @RequestParam(required = false) invoiceId: UUID?,
        @RequestParam(required = false) expenseId: UUID?,
    ): List<PaymentResponse> = when {
        invoiceId != null -> paymentService.listByInvoice(invoiceId)
        expenseId != null -> paymentService.listByExpense(expenseId)
        else -> emptyList()
    }
}
