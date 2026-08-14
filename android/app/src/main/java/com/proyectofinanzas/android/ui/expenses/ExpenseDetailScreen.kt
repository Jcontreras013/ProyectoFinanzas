package com.proyectofinanzas.android.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun ExpenseDetailScreen(expenseId: String, viewModel: ExpenseDetailViewModel = viewModel()) {
    LaunchedEffect(expenseId) { viewModel.load(expenseId) }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val current = state) {
            is UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Success -> {
                val expense = current.data
                Text("Gasto #${expense.expenseNumber}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusChip(expense.status, modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Cuenta: ${expense.accountName}", modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Monto: ${formatMoney(expense.amount, expense.currency)}", modifier = Modifier.weight(1f))
                    Text("Saldo: ${formatMoney(expense.balanceInBase)}", modifier = Modifier.weight(1f))
                }
                expense.partyName?.let {
                    Text("Proveedor: $it", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
