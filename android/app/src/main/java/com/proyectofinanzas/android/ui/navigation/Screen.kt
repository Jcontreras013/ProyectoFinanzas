package com.proyectofinanzas.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Accounts : Screen("accounts")

    data object JournalEntries : Screen("journal_entries")
    data object JournalEntryNew : Screen("journal_entries/new")
    data object JournalEntryDetail : Screen("journal_entries/{entryId}") {
        fun of(entryId: String) = "journal_entries/$entryId"
    }

    data object Invoices : Screen("invoices")
    data object InvoiceDetail : Screen("invoices/{invoiceId}") {
        fun of(invoiceId: String) = "invoices/$invoiceId"
    }

    data object Expenses : Screen("expenses")
    data object ExpenseNew : Screen("expenses/new")
    data object ExpenseDetail : Screen("expenses/{expenseId}") {
        fun of(expenseId: String) = "expenses/$expenseId"
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Panel", Icons.Filled.SpaceDashboard),
    BottomNavItem(Screen.Accounts, "Cuentas", Icons.Filled.AccountBalanceWallet),
    BottomNavItem(Screen.JournalEntries, "Asientos", Icons.Filled.Receipt),
    BottomNavItem(Screen.Invoices, "Facturas", Icons.Filled.Description),
    BottomNavItem(Screen.Expenses, "Gastos", Icons.Filled.RequestQuote),
)
