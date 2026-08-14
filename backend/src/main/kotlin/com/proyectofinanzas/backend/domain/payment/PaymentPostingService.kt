package com.proyectofinanzas.backend.domain.payment

import com.proyectofinanzas.backend.common.Currency
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.account.AccountRepository
import com.proyectofinanzas.backend.domain.account.AccountSystemRole
import com.proyectofinanzas.backend.domain.journal.JournalEntry
import com.proyectofinanzas.backend.domain.journal.JournalSourceType
import com.proyectofinanzas.backend.domain.journal.PostingLine
import com.proyectofinanzas.backend.domain.journal.PostingService
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Contabiliza un pago ya persistido:
 *   Cobro de factura: Dr Caja/Banco (HNL o USD)  / Cr Cuentas por Cobrar
 *   Pago de gasto a crédito: Dr Cuentas por Pagar / Cr Caja/Banco (HNL o USD)
 */
@Service
class PaymentPostingService(
    private val accountRepository: AccountRepository,
    private val postingService: PostingService,
) {

    fun post(payment: Payment, createdById: UUID): JournalEntry {
        val cashAccount = accountRepository.findBySystemRole(
            if (payment.currency == Currency.USD) AccountSystemRole.CASH_USD else AccountSystemRole.CASH_HNL
        ).orElseThrow { NotFoundException("No hay una cuenta de caja/banco configurada para ${payment.currency}") }

        val invoice = payment.invoice
        val lines = if (invoice != null) {
            val arAccount = accountRepository.findBySystemRole(AccountSystemRole.ACCOUNTS_RECEIVABLE)
                .orElseThrow { NotFoundException("No hay una cuenta configurada con rol ACCOUNTS_RECEIVABLE") }
            listOf(
                PostingLine(
                    accountId = requireNotNull(cashAccount.id),
                    partyId = invoice.party.id,
                    debit = payment.amountInBase,
                    description = "Cobro factura #${invoice.invoiceNumber}",
                ),
                PostingLine(
                    accountId = requireNotNull(arAccount.id),
                    partyId = invoice.party.id,
                    credit = payment.amountInBase,
                    description = "Cobro factura #${invoice.invoiceNumber}",
                ),
            )
        } else {
            val expense = requireNotNull(payment.expense)
            val apAccount = accountRepository.findBySystemRole(AccountSystemRole.ACCOUNTS_PAYABLE)
                .orElseThrow { NotFoundException("No hay una cuenta configurada con rol ACCOUNTS_PAYABLE") }
            listOf(
                PostingLine(
                    accountId = requireNotNull(apAccount.id),
                    partyId = expense.party?.id,
                    debit = payment.amountInBase,
                    description = "Pago gasto #${expense.expenseNumber}",
                ),
                PostingLine(
                    accountId = requireNotNull(cashAccount.id),
                    partyId = expense.party?.id,
                    credit = payment.amountInBase,
                    description = "Pago gasto #${expense.expenseNumber}",
                ),
            )
        }

        return postingService.post(
            entryDate = payment.paymentDate,
            description = if (invoice != null) {
                "Cobro factura #${invoice.invoiceNumber}"
            } else {
                "Pago gasto #${requireNotNull(payment.expense).expenseNumber}"
            },
            sourceType = JournalSourceType.PAYMENT,
            sourceId = payment.id,
            lines = lines,
            createdById = createdById,
        )
    }
}
