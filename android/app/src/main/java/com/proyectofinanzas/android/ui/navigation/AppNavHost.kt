package com.proyectofinanzas.android.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.ui.accounts.AccountsScreen
import com.proyectofinanzas.android.ui.auth.LoginScreen
import com.proyectofinanzas.android.ui.dashboard.DashboardScreen
import com.proyectofinanzas.android.ui.expenses.ExpenseCreateScreen
import com.proyectofinanzas.android.ui.expenses.ExpenseDetailScreen
import com.proyectofinanzas.android.ui.expenses.ExpensesListScreen
import com.proyectofinanzas.android.ui.invoices.InvoiceDetailScreen
import com.proyectofinanzas.android.ui.invoices.InvoicesListScreen
import com.proyectofinanzas.android.ui.journalentries.JournalEntriesListScreen
import com.proyectofinanzas.android.ui.journalentries.JournalEntryCreateScreen
import com.proyectofinanzas.android.ui.journalentries.JournalEntryDetailScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost() {
    val sessionManager = AppContainer.sessionManager
    val isAuthenticated by sessionManager.isAuthenticated.collectAsState()

    LaunchedEffect(Unit) { sessionManager.checkInitialAuth() }

    when (isAuthenticated) {
        null -> Unit // esperando a resolver la sesión guardada
        false -> LoginScreen()
        true -> MainScaffold()
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination
    val userName by AppContainer.sessionManager.currentUserName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sistema Contable")
                        userName?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { AppContainer.sessionManager.logout() } }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Accounts.route) { AccountsScreen() }

            composable(Screen.JournalEntries.route) {
                JournalEntriesListScreen(
                    onOpenEntry = { navController.navigate(Screen.JournalEntryDetail.of(it)) },
                    onCreateEntry = { navController.navigate(Screen.JournalEntryNew.route) },
                )
            }
            composable(Screen.JournalEntryNew.route) {
                JournalEntryCreateScreen(onCreated = {
                    navController.popBackStack()
                    navController.navigate(Screen.JournalEntryDetail.of(it))
                })
            }
            composable(Screen.JournalEntryDetail.route) { backStack ->
                val entryId = backStack.arguments?.getString("entryId").orEmpty()
                JournalEntryDetailScreen(entryId)
            }

            composable(Screen.Invoices.route) {
                InvoicesListScreen(onOpenInvoice = { navController.navigate(Screen.InvoiceDetail.of(it)) })
            }
            composable(Screen.InvoiceDetail.route) { backStack ->
                val invoiceId = backStack.arguments?.getString("invoiceId").orEmpty()
                InvoiceDetailScreen(invoiceId)
            }

            composable(Screen.Expenses.route) {
                ExpensesListScreen(
                    onOpenExpense = { navController.navigate(Screen.ExpenseDetail.of(it)) },
                    onCreateExpense = { navController.navigate(Screen.ExpenseNew.route) },
                )
            }
            composable(Screen.ExpenseNew.route) {
                ExpenseCreateScreen(onCreated = {
                    navController.popBackStack()
                    navController.navigate(Screen.ExpenseDetail.of(it))
                })
            }
            composable(Screen.ExpenseDetail.route) { backStack ->
                val expenseId = backStack.arguments?.getString("expenseId").orEmpty()
                ExpenseDetailScreen(expenseId)
            }
        }
    }
}
