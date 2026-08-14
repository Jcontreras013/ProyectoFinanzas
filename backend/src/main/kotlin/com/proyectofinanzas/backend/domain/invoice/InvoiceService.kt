package com.proyectofinanzas.backend.domain.invoice

import com.proyectofinanzas.backend.common.BusinessRuleException
import com.proyectofinanzas.backend.common.Currency
import com.proyectofinanzas.backend.common.MoneyUtils
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.account.AccountRepository
import com.proyectofinanzas.backend.domain.exchangerate.ExchangeRateService
import com.proyectofinanzas.backend.domain.journal.PostingService
import com.proyectofinanzas.backend.domain.party.PartyRepository
import com.proyectofinanzas.backend.domain.payment.PaymentRepository
import com.proyectofinanzas.backend.domain.user.UserRepository
import com.proyectofinanzas.backend.security.SecurityUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceLineRepository: InvoiceLineRepository,
    private val partyRepository: PartyRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val exchangeRateService: ExchangeRateService,
    private val paymentRepository: PaymentRepository,
    private val invoicePostingService: InvoicePostingService,
    private val postingService: PostingService,
) {

    fun create(request: CreateInvoiceRequest): InvoiceResponse {
        if (request.dueDate.isBefore(request.issueDate)) {
            throw BusinessRuleException("La fecha de vencimiento no puede ser anterior a la fecha de emisión")
        }
        val party = partyRepository.findById(request.partyId)
            .orElseThrow { NotFoundException("Tercero no encontrado") }
        val createdBy = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow { NotFoundException("Usuario no encontrado") }

        val exchangeRate = if (request.currency == Currency.HNL) {
            BigDecimal.ONE
        } else {
            request.exchangeRate ?: exchangeRateService.rateFor(request.issueDate)
        }

        val accountsById = request.lines.map { it.accountId }.distinct()
            .associateWith { accountId ->
                accountRepository.findById(accountId)
                    .orElseThrow { NotFoundException("Cuenta no encontrada: $accountId") }
            }

        val lineTotals = request.lines.map { MoneyUtils.round(it.quantity * it.unitPrice) }
        val subtotal = lineTotals.fold(BigDecimal.ZERO) { acc, v -> acc + v }
        val taxAmount = MoneyUtils.round(
            request.lines.zip(lineTotals).fold(BigDecimal.ZERO) { acc, (line, total) ->
                acc + total * line.taxRate / BigDecimal(100)
            }
        )
        val total = subtotal + taxAmount
        val amountInBase = MoneyUtils.toBase(total, exchangeRate)

        val invoice = Invoice(
            party = party,
            issueDate = request.issueDate,
            dueDate = request.dueDate,
            currency = request.currency,
            exchangeRate = exchangeRate,
            subtotal = subtotal,
            taxAmount = taxAmount,
            total = total,
            amountInBase = amountInBase,
            status = InvoiceStatus.ISSUED,
            notes = request.notes,
            createdBy = createdBy,
        )
        val savedInvoice = invoiceRepository.save(invoice)

        val lines = request.lines.mapIndexed { index, lineRequest ->
            invoiceLineRepository.save(
                InvoiceLine(
                    invoice = savedInvoice,
                    lineNumber = index + 1,
                    description = lineRequest.description,
                    quantity = lineRequest.quantity,
                    unitPrice = lineRequest.unitPrice,
                    taxRate = lineRequest.taxRate,
                    lineTotal = lineTotals[index],
                    account = accountsById.getValue(lineRequest.accountId),
                )
            )
        }

        val journalEntry = invoicePostingService.post(savedInvoice, lines, SecurityUtils.currentUserId())
        savedInvoice.journalEntry = journalEntry

        return toResponse(invoiceRepository.save(savedInvoice), lines)
    }

    fun cancel(id: UUID): InvoiceResponse {
        val invoice = findEntity(id)
        if (invoice.status == InvoiceStatus.CANCELLED) {
            throw BusinessRuleException("La factura ya está cancelada")
        }
        val paid = paymentRepository.sumAmountInBaseByInvoiceId(id)
        if (paid.signum() > 0) {
            throw BusinessRuleException("No se puede cancelar una factura con cobros registrados")
        }
        val journalEntry = invoice.journalEntry
        if (journalEntry != null) {
            postingService.reverse(
                original = journalEntry,
                entryDate = LocalDate.now(),
                reason = "Cancelación de factura #${invoice.invoiceNumber}",
                createdById = SecurityUtils.currentUserId(),
            )
        }
        invoice.status = InvoiceStatus.CANCELLED
        return toResponse(invoiceRepository.save(invoice), lines(id))
    }

    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<InvoiceResponse> =
        invoiceRepository.findAllByOrderByIssueDateDescInvoiceNumberDesc(pageable)
            .map { toResponse(it, lines(requireNotNull(it.id))) }

    @Transactional(readOnly = true)
    fun get(id: UUID): InvoiceResponse = toResponse(findEntity(id), lines(id))

    private fun lines(invoiceId: UUID) = invoiceLineRepository.findByInvoiceIdOrderByLineNumberAsc(invoiceId)

    private fun findEntity(id: UUID): Invoice =
        invoiceRepository.findById(id).orElseThrow { NotFoundException("Factura no encontrada") }

    private fun toResponse(invoice: Invoice, lines: List<InvoiceLine>): InvoiceResponse {
        val paid = paymentRepository.sumAmountInBaseByInvoiceId(requireNotNull(invoice.id))
        return InvoiceResponse(
            id = requireNotNull(invoice.id),
            invoiceNumber = invoice.invoiceNumber,
            partyId = requireNotNull(invoice.party.id),
            partyName = invoice.party.name,
            issueDate = invoice.issueDate,
            dueDate = invoice.dueDate,
            currency = invoice.currency,
            exchangeRate = invoice.exchangeRate,
            subtotal = invoice.subtotal,
            taxAmount = invoice.taxAmount,
            total = invoice.total,
            amountInBase = invoice.amountInBase,
            paidInBase = paid,
            balanceInBase = invoice.amountInBase - paid,
            status = invoice.status,
            journalEntryId = invoice.journalEntry?.id,
            notes = invoice.notes,
            createdAt = requireNotNull(invoice.createdAt),
            lines = lines.map {
                InvoiceLineResponse(
                    id = requireNotNull(it.id),
                    lineNumber = it.lineNumber,
                    description = it.description,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    taxRate = it.taxRate,
                    lineTotal = it.lineTotal,
                    accountId = requireNotNull(it.account.id),
                    accountName = it.account.name,
                )
            },
        )
    }
}
