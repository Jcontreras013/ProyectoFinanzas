package com.proyectofinanzas.backend.domain.invoice

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService,
) {
    @GetMapping
    fun list(pageable: Pageable): Page<InvoiceResponse> = invoiceService.list(pageable)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): InvoiceResponse = invoiceService.get(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun create(@Valid @RequestBody request: CreateInvoiceRequest): InvoiceResponse = invoiceService.create(request)

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun cancel(@PathVariable id: UUID): InvoiceResponse = invoiceService.cancel(id)
}
