package com.proyectofinanzas.backend.domain.report

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService,
) {

    @GetMapping("/trial-balance")
    fun trialBalance(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) asOf: LocalDate?,
    ): TrialBalanceResponse = reportService.trialBalance(asOf ?: LocalDate.now())

    @GetMapping("/balance-sheet")
    fun balanceSheet(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) asOf: LocalDate?,
    ): BalanceSheetResponse = reportService.balanceSheet(asOf ?: LocalDate.now())

    @GetMapping("/income-statement")
    fun incomeStatement(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): IncomeStatementResponse {
        val effectiveTo = to ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.withDayOfMonth(1)
        return reportService.incomeStatement(effectiveFrom, effectiveTo)
    }

    @GetMapping("/general-ledger/{accountId}")
    fun generalLedger(
        @PathVariable accountId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): GeneralLedgerResponse {
        val effectiveTo = to ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.withDayOfMonth(1)
        return reportService.generalLedger(accountId, effectiveFrom, effectiveTo)
    }

    @GetMapping("/dashboard-kpis")
    fun dashboardKpis(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): DashboardKpisResponse {
        val effectiveTo = to ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.withDayOfMonth(1)
        return reportService.dashboardKpis(effectiveFrom, effectiveTo)
    }
}
