package com.proyectofinanzas.android.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectofinanzas.android.ui.common.AccountPicker

private val paymentMethods = listOf("BANK" to "Banco", "CASH" to "Efectivo", "CREDIT" to "Crédito")

@Composable
fun ExpenseCreateScreen(
    onCreated: (String) -> Unit,
    viewModel: ExpenseCreateViewModel = viewModel(),
) {
    LaunchedEffect(viewModel.createdExpenseId) {
        viewModel.createdExpenseId?.let(onCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuevo gasto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
        )
        AccountPicker(
            label = "Cuenta de gasto",
            accounts = viewModel.accounts,
            selectedAccountId = viewModel.accountId,
            onSelect = { viewModel.accountId = it },
        )
        OutlinedTextField(
            value = viewModel.expenseDate,
            onValueChange = { viewModel.expenseDate = it },
            label = { Text("Fecha (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = viewModel.amount,
            onValueChange = { viewModel.amount = it },
            label = { Text("Monto (Lempiras)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Forma de pago", style = MaterialTheme.typography.titleSmall)
        paymentMethods.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = viewModel.paymentMethod == value,
                        onClick = { viewModel.paymentMethod = value },
                    ),
            ) {
                RadioButton(selected = viewModel.paymentMethod == value, onClick = { viewModel.paymentMethod = value })
                Text(label, modifier = Modifier.padding(start = 8.dp, top = 12.dp))
            }
        }

        viewModel.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = viewModel::submit,
            enabled = viewModel.canSubmit() && !viewModel.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (viewModel.isSaving) "Guardando..." else "Registrar gasto")
        }
    }
}
