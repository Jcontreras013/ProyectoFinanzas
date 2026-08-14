package com.proyectofinanzas.backend.domain.report

import com.proyectofinanzas.backend.common.MoneyUtils
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.account.Account
import com.proyectofinanzas.backend.domain.account.AccountRepository
import com.proyectofinanzas.backend.domain.account.AccountSystemRole
import com.proyectofinanzas.backend.domain.account.AccountType
import com.proyectofinanzas.backend.domain.journal.JournalEntryLineRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

private data class DebitCredit(val debit: BigDecimal, val credit: BigDecimal)

@Service
@Transactional(readOnly = true)
class ReportService(
    private val accountRepository: AccountRepository,
    private val journalEntryLineRepository: JournalEntryLineRepository,
) {

    fun trialBalance(asOf: LocalDate): TrialBalanceResponse {
        val figures = movementsUpTo(asOf)
        val accounts = accountRepository.findAllByOrderByCodeAsc()
        val lines = accounts.mapNotNull { account ->
            val fig = figures[account.id] ?: return@mapNotNull null
            TrialBalanceLine(
                accountId = requireNotNull(account.id),
                code = account.code,
                name = account.name,
                type = account.type,
                debit = fig.debit,
                credit = fig.credit,
            )
        }
        return TrialBalanceResponse(
            asOf = asOf,
            lines = lines,
            totalDebit = lines.fold(BigDecimal.ZERO) { acc, l -> acc + l.debit },
            totalCredit = lines.fold(BigDecimal.ZERO) { acc, l -> acc + l.credit },
        )
    }

    fun balanceSheet(asOf: LocalDate): BalanceSheetResponse {
        val figures = movementsUpTo(asOf)
        val accounts = accountRepository.findAllByOrderByCodeAsc()
        val byParent = accounts.groupBy { it.parent?.id }

        fun rollup(account: Account): BigDecimal {
            val own = figures[account.id]?.let { normalBalance(account.type, it.debit, it.credit) } ?: BigDecimal.ZERO
            val childrenTotal = byParent[account.id].orEmpty().fold(BigDecimal.ZERO) { acc, child -> acc + rollup(child) }
            return own + childrenTotal
        }

        fun linesFor(type: AccountType): List<BalanceSheetLine> =
            accounts.filter { it.type == type }.map {
                BalanceSheetLine(
                    accountId = requireNotNull(it.id),
                    code = it.code,
                    name = it.name,
                    parentId = it.parent?.id,
                    balance = rollup(it),
                )
            }

        fun totalForRoots(type: AccountType): BigDecimal =
            accounts.filter { it.type == type && it.parent == null }
                .fold(BigDecimal.ZERO) { acc, a -> acc + rollup(a) }

        val totalAssets = totalForRoots(AccountType.ASSET)
        val totalLiabilities = totalForRoots(AccountType.LIABILITY)
        val totalEquity = totalForRoots(AccountType.EQUITY)

        val yearStart = LocalDate.of(asOf.year, 1, 1)
        val ytdIncomeExpense = movementsBetween(yearStart, asOf)
        val currentYearEarnings = accounts
            .filter { it.type == AccountType.INCOME || it.type == AccountType.EXPENSE }
            .fold(BigDecimal.ZERO) { acc, a ->
                val fig = ytdIncomeExpense[a.id] ?: return@fold acc
                acc + normalBalance(a.type, fig.debit, fig.credit).let {
                    if (a.type == AccountType.INCOME) it else it.negate()
                }
            }

        val isBalanced = totalAssets.compareTo(totalLiabilities + totalEquity + currentYearEarnings) == 0

        return BalanceSheetResponse(
            asOf = asOf,
            assets = linesFor(AccountType.ASSET),
            liabilities = linesFor(AccountType.LIABILITY),
            equity = linesFor(AccountType.EQUITY),
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            totalEquity = totalEquity,
            currentYearEarnings = currentYearEarnings,
            isBalanced = isBalanced,
        )
    }

    fun incomeStatement(from: LocalDate, to: LocalDate): IncomeStatementResponse {
        val figures = movementsBetween(from, to)
        val accounts = accountRepository.findAllByOrderByCodeAsc()

        fun linesFor(type: AccountType): List<IncomeStatementLine> =
            accounts.filter { it.type == type }.mapNotNull { account ->
                val fig = figures[account.id] ?: return@mapNotNull null
                val amount = normalBalance(type, fig.debit, fig.credit)
                if (amount.signum() == 0) return@mapNotNull null
                IncomeStatementLine(requireNotNull(account.id), account.code, account.name, amount)
            }

        val income = linesFor(AccountType.INCOME)
        val expenses = linesFor(AccountType.EXPENSE)
        val totalIncome = income.fold(BigDecimal.ZERO) { acc, l -> acc + l.amount }
        val totalExpenses = expenses.fold(BigDecimal.ZERO) { acc, l -> acc + l.amount }

        return IncomeStatementResponse(
            from = from,
            to = to,
            income = income,
            expenses = expenses,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netIncome = totalIncome - totalExpenses,
        )
    }

    fun generalLedger(accountId: UUID, from: LocalDate, to: LocalDate): GeneralLedgerResponse {
        val account = accountRepository.findById(accountId).orElseThrow { NotFoundException("Cuenta no encontrada") }
        val openingFigures = movementsUpTo(from.minusDays(1))[accountId]
        val opening = openingFigures?.let { normalBalance(account.type, it.debit, it.credit) } ?: BigDecimal.ZERO

        val movements = journalEntryLineRepository
            .findByAccountIdAndJournalEntry_EntryDateBetweenOrderByJournalEntry_EntryDateAsc(accountId, from, to)

        var running = opening
        val lines = movements.map {
            running += if (account.type == AccountType.ASSET || account.type == AccountType.EXPENSE) {
                it.debit - it.credit
            } else {
                it.credit - it.debit
            }
            GeneralLedgerLine(
                journalEntryId = requireNotNull(it.journalEntry.id),
                entryNumber = it.journalEntry.entryNumber,
                entryDate = it.journalEntry.entryDate,
                description = it.description ?: it.journalEntry.description,
                debit = it.debit,
                credit = it.credit,
                runningBalance = running,
            )
        }

        return GeneralLedgerResponse(
            accountId = accountId,
            accountCode = account.code,
            accountName = account.name,
            from = from,
            to = to,
            openingBalance = opening,
            lines = lines,
            closingBalance = running,
        )
    }

    fun dashboardKpis(from: LocalDate, to: LocalDate): DashboardKpisResponse {
        val periodFigures = movementsBetween(from, to)
        val cumulativeFigures = movementsUpTo(to)
        val accounts = accountRepository.findAllByOrderByCodeAsc()

        fun periodTotal(type: AccountType): BigDecimal =
            accounts.filter { it.type == type }.fold(BigDecimal.ZERO) { acc, a ->
                val fig = periodFigures[a.id] ?: return@fold acc
                acc + normalBalance(type, fig.debit, fig.credit)
            }

        fun systemRoleBalance(role: AccountSystemRole): BigDecimal {
            val account = accountRepository.findBySystemRole(role).orElse(null) ?: return BigDecimal.ZERO
            val fig = cumulativeFigures[account.id] ?: return BigDecimal.ZERO
            return normalBalance(account.type, fig.debit, fig.credit)
        }

        val revenue = periodTotal(AccountType.INCOME)
        val expense = periodTotal(AccountType.EXPENSE)

        val monthlySeries = (5 downTo 0).map { offset ->
            val month = YearMonth.from(to).minusMonths(offset.toLong())
            val monthFigures = movementsBetween(month.atDay(1), month.atEndOfMonth())
            val monthRevenue = accounts.filter { it.type == AccountType.INCOME }.fold(BigDecimal.ZERO) { acc, a ->
                val fig = monthFigures[a.id] ?: return@fold acc
                acc + normalBalance(AccountType.INCOME, fig.debit, fig.credit)
            }
            val monthExpense = accounts.filter { it.type == AccountType.EXPENSE }.fold(BigDecimal.ZERO) { acc, a ->
                val fig = monthFigures[a.id] ?: return@fold acc
                acc + normalBalance(AccountType.EXPENSE, fig.debit, fig.credit)
            }
            MonthlyPoint(
                month = month.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")).replaceFirstChar { it.uppercase() } +
                    " " + month.year,
                revenue = monthRevenue,
                expense = monthExpense,
            )
        }

        val expenseByCategory = accounts.filter { it.type == AccountType.EXPENSE }.mapNotNull { account ->
            val fig = periodFigures[account.id] ?: return@mapNotNull null
            val amount = normalBalance(AccountType.EXPENSE, fig.debit, fig.credit)
            if (amount.signum() <= 0) return@mapNotNull null
            CategoryPoint(account.name, amount)
        }.sortedByDescending { it.amount }

        return DashboardKpisResponse(
            from = from,
            to = to,
            revenueInPeriod = revenue,
            expensesInPeriod = expense,
            netIncomeInPeriod = revenue - expense,
            cashBalanceHnl = systemRoleBalance(AccountSystemRole.CASH_HNL),
            cashBalanceUsd = systemRoleBalance(AccountSystemRole.CASH_USD),
            accountsReceivableOutstanding = systemRoleBalance(AccountSystemRole.ACCOUNTS_RECEIVABLE),
            accountsPayableOutstanding = systemRoleBalance(AccountSystemRole.ACCOUNTS_PAYABLE),
            monthlySeries = monthlySeries,
            expenseByCategory = expenseByCategory,
        )
    }

    private fun normalBalance(type: AccountType, debit: BigDecimal, credit: BigDecimal): BigDecimal =
        when (type) {
            AccountType.ASSET, AccountType.EXPENSE -> debit - credit
            AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> credit - debit
        }

    private fun movementsUpTo(asOf: LocalDate): Map<UUID, DebitCredit> =
        journalEntryLineRepository.trialBalanceUpTo(asOf).associate {
            (it[0] as UUID) to DebitCredit(MoneyUtils.round(it[1] as BigDecimal), MoneyUtils.round(it[2] as BigDecimal))
        }

    private fun movementsBetween(from: LocalDate, to: LocalDate): Map<UUID, DebitCredit> =
        journalEntryLineRepository.trialBalanceBetween(from, to).associate {
            (it[0] as UUID) to DebitCredit(MoneyUtils.round(it[1] as BigDecimal), MoneyUtils.round(it[2] as BigDecimal))
        }
}
