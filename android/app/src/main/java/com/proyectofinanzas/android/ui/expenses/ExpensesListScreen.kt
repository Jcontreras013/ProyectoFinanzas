package com.proyectofinanzas.android.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectofinanzas.android.ui.common.ErrorBox
import com.proyectofinanzas.android.ui.common.LoadingBox
import com.proyectofinanzas.android.ui.common.StatusChip
import com.proyectofinanzas.android.ui.common.UiState
import com.proyectofinanzas.android.ui.common.formatMoney

@Composable
fun ExpensesListScreen(
    onOpenExpense: (String) -> Unit,
    onCreateExpense: () -> Unit,
    viewModel: ExpensesListViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo gasto")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Gastos", style = MaterialTheme.typography.headlineSmall)

            when (val current = state) {
                is UiState.Loading -> LoadingBox()
                is UiState.Error -> ErrorBox(current.message)
                is UiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 12.dp),
                ) {
                    items(current.data) { expense ->
                        ListItem(
                            modifier = Modifier.clickable { onOpenExpense(expense.id) },
                            headlineContent = { Text(expense.description) },
                            overlineContent = { Text("#${expense.expenseNumber} · ${expense.expenseDate}") },
                            supportingContent = { Text(formatMoney(expense.amount, expense.currency)) },
                            trailingContent = { StatusChip(expense.status) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
