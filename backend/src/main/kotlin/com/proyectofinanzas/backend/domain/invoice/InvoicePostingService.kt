package com.proyectofinanzas.backend.domain.invoice

import com.proyectofinanzas.backend.common.BusinessRuleException
import com.proyectofinanzas.backend.common.MoneyUtils
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.account.AccountRepository
import com.proyectofinanzas.backend.domain.account.AccountSystemRole
import com.proyectofinanzas.backend.domain.journal.JournalEntry
import com.proyectofinanzas.backend.domain.journal.JournalSourceType
import com.proyectofinanzas.backend.domain.journal.PostingLine
import com.proyectofinanzas.backend.domain.journal.PostingService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

/**
 * Contabiliza una factura ya persistida:
 *   Dr Cuentas por Cobrar         total (en HNL)
 *   Cr Ingresos por venta (x cta) subtotal (en HNL, por cuenta de cada línea)
 *   Cr ISV por pagar              impuesto (en HNL, si > 0)
 *
 * El desglose de ingresos por cuenta se redondea línea a línea, y la última cuenta del
 * grupo absorbe el remanente de redondeo para que la suma cuadre exactamente contra
 * amountInBase (ver riesgo de precisión documentado en el plan).
 */
@Service
class InvoicePostingService(
    private val accountRepository: AccountRepository,
    private val postingService: PostingService,
) {

    fun post(invoice: Invoice, lines: List<InvoiceLine>, createdById: UUID): JournalEntry {
        val arAccount = accountRepository.findBySystemRole(AccountSystemRole.ACCOUNTS_RECEIVABLE)
            .orElseThrow { NotFoundException("No hay una cuenta configurada con rol ACCOUNTS_RECEIVABLE") }

        val revenueByAccount = lines.groupBy { it.account }
            .mapValues { (_, groupLines) -> groupLines.fold(BigDecimal.ZERO) { acc, l -> acc + l.lineTotal } }

        val taxInBase = MoneyUtils.toBase(invoice.taxAmount, invoice.exchangeRate)
        val revenueInBaseTotal = invoice.amountInBase - taxInBase
        if (revenueInBaseTotal.signum() < 0) {
            throw BusinessRuleException("El monto de impuesto no puede superar el total de la factura")
        }

        val accountsSorted = revenueByAccount.entries.sortedBy { it.key.id.toString() }
        var runningTotal = BigDecimal.ZERO
        val postingLines = mutableListOf<PostingLine>()
        accountsSorted.forEachIndexed { index, (account, originalAmount) ->
            val isLast = index == accountsSorted.lastIndex
            val amountInBase = if (isLast) {
                revenueInBaseTotal - runningTotal
            } else {
                MoneyUtils.toBase(originalAmount, invoice.exchangeRate)
            }
            runningTotal += amountInBase
            postingLines.add(
                PostingLine(
                    accountId = requireNotNull(account.id),
                    partyId = invoice.party.id,
                    credit = amountInBase,
                    description = "Venta factura #${invoice.invoiceNumber}",
                )
            )
        }

        if (taxInBase.signum() > 0) {
            val taxAccount = accountRepository.findBySystemRole(AccountSystemRole.TAX_PAYABLE)
                .orElseThrow { NotFoundException("No hay una cuenta configurada con rol TAX_PAYABLE") }
            postingLines.add(
                PostingLine(
                    accountId = requireNotNull(taxAccount.id),
                    partyId = invoice.party.id,
                    credit = taxInBase,
                    description = "ISV factura #${invoice.invoiceNumber}",
                )
            )
        }

        postingLines.add(
            0,
            PostingLine(
                accountId = requireNotNull(arAccount.id),
                partyId = invoice.party.id,
                debit = invoice.amountInBase,
                description = "CxC factura #${invoice.invoiceNumber}",
            )
        )

        return postingService.post(
            entryDate = invoice.issueDate,
            description = "Factura #${invoice.invoiceNumber} - ${invoice.party.name}",
            sourceType = JournalSourceType.INVOICE,
            sourceId = invoice.id,
            lines = postingLines,
            createdById = createdById,
        )
    }
}
