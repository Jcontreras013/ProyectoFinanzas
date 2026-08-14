package com.proyectofinanzas.backend.domain.expense

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
 * Contabiliza un gasto ya persistido:
 *   Dr Cuenta de gasto                total
 *   Cr Caja/Banco (HNL o USD según moneda del gasto)   -- si es CASH o BANK
 *   Cr Cuentas por Pagar                                -- si es CREDIT
 */
@Service
class ExpensePostingService(
    private val accountRepository: AccountRepository,
    private val postingService: PostingService,
) {

    fun post(expense: Expense, createdById: UUID): JournalEntry {
        val creditAccount = when (expense.paymentMethod) {
            ExpensePaymentMethod.CREDIT ->
                accountRepository.findBySystemRole(AccountSystemRole.ACCOUNTS_PAYABLE)
                    .orElseThrow { NotFoundException("No hay una cuenta configurada con rol ACCOUNTS_PAYABLE") }
            ExpensePaymentMethod.CASH, ExpensePaymentMethod.BANK ->
                accountRepository.findBySystemRole(
                    if (expense.currency == Currency.USD) AccountSystemRole.CASH_USD else AccountSystemRole.CASH_HNL
                ).orElseThrow { NotFoundException("No hay una cuenta de caja/banco configurada para ${expense.currency}") }
        }

        val lines = listOf(
            PostingLine(
                accountId = requireNotNull(expense.account.id),
                partyId = expense.party?.id,
                debit = expense.amountInBase,
                description = "Gasto #${expense.expenseNumber}",
            ),
            PostingLine(
                accountId = requireNotNull(creditAccount.id),
                partyId = expense.party?.id,
                credit = expense.amountInBase,
                description = "Gasto #${expense.expenseNumber}",
            ),
        )

        return postingService.post(
            entryDate = expense.expenseDate,
            description = "Gasto #${expense.expenseNumber} - ${expense.description}",
            sourceType = JournalSourceType.EXPENSE,
            sourceId = expense.id,
            lines = lines,
            createdById = createdById,
        )
    }
}
