package com.proyectofinanzas.android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: UserDto)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val active: Boolean,
)

@Serializable
data class AccountDto(
    val id: String,
    val code: String,
    val name: String,
    val type: String,
    val parentId: String? = null,
    val allowsPosting: Boolean,
    val systemRole: String? = null,
    val isActive: Boolean,
)

@Serializable
data class JournalEntryLineDto(
    val id: String,
    val lineNumber: Int,
    val accountId: String,
    val accountCode: String,
    val accountName: String,
    val partyId: String? = null,
    val partyName: String? = null,
    val debit: String,
    val credit: String,
    val description: String? = null,
)

@Serializable
data class JournalEntryDto(
    val id: String,
    val entryNumber: Long,
    val entryDate: String,
    val description: String,
    val sourceType: String,
    val createdByName: String,
    val createdAt: String,
    val lines: List<JournalEntryLineDto>,
)

@Serializable
data class JournalEntryLineRequest(
    val accountId: String,
    val debit: String,
    val credit: String,
)

@Serializable
data class CreateJournalEntryRequest(
    val entryDate: String,
    val description: String,
    val lines: List<JournalEntryLineRequest>,
)

@Serializable
data class InvoiceLineDto(
    val id: String,
    val description: String,
    val quantity: String,
    val unitPrice: String,
    val taxRate: String,
    val lineTotal: String,
)

@Serializable
data class InvoiceDto(
    val id: String,
    val invoiceNumber: Long,
    val partyName: String,
    val issueDate: String,
    val dueDate: String,
    val currency: String,
    val total: String,
    val paidInBase: String,
    val balanceInBase: String,
    val status: String,
    val lines: List<InvoiceLineDto>,
)

@Serializable
data class ExpenseDto(
    val id: String,
    val expenseNumber: Long,
    val partyName: String? = null,
    val expenseDate: String,
    val currency: String,
    val accountName: String,
    val description: String,
    val paymentMethod: String,
    val amount: String,
    val balanceInBase: String,
    val status: String,
)

@Serializable
data class CreateExpenseRequest(
    val partyId: String? = null,
    val expenseDate: String,
    val currency: String,
    val accountId: String,
    val description: String,
    val paymentMethod: String,
    val amount: String,
)

@Serializable
data class DashboardKpisDto(
    val from: String,
    val to: String,
    val revenueInPeriod: String,
    val expensesInPeriod: String,
    val netIncomeInPeriod: String,
    val cashBalanceHnl: String,
    val cashBalanceUsd: String,
    val accountsReceivableOutstanding: String,
    val accountsPayableOutstanding: String,
)

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
)

@Serializable
data class ApiErrorDto(
    val status: Int = 0,
    val error: String = "",
    val message: String = "Error inesperado",
    @SerialName("fieldErrors") val fieldErrors: Map<String, String>? = null,
)
