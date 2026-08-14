package com.proyectofinanzas.android.ui.journalentries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectofinanzas.android.ui.common.AccountPicker
import com.proyectofinanzas.android.ui.common.formatMoney

@Composable
fun JournalEntryCreateScreen(
    onCreated: (String) -> Unit,
    viewModel: JournalEntryCreateViewModel = viewModel(),
) {
    LaunchedEffect(viewModel.createdEntryId) {
        viewModel.createdEntryId?.let(onCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuevo asiento contable", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = viewModel.entryDate,
            onValueChange = { viewModel.entryDate = it },
            label = { Text("Fecha (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Líneas", style = MaterialTheme.typography.titleMedium)

        viewModel.lines.forEachIndexed { index, line ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountPicker(
                        label = "Cuenta",
                        accounts = viewModel.accounts,
                        selectedAccountId = line.accountId,
                        onSelect = { line.accountId = it },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = line.debit,
                            onValueChange = { line.debit = it; if (it.isNotBlank()) line.credit = "" },
                            label = { Text("Débito") },
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = line.credit,
                            onValueChange = { line.credit = it; if (it.isNotBlank()) line.debit = "" },
                            label = { Text("Crédito") },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.removeLine(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar línea")
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = viewModel::addLine) { Text("Agregar línea") }

        val balanced = viewModel.isBalanced()
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (balanced) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        ) {
            Text(
                "Débitos: ${formatMoney(viewModel.totalDebit().toString())} · Créditos: ${formatMoney(viewModel.totalCredit().toString())}",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        viewModel.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = viewModel::submit,
            enabled = balanced && !viewModel.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (viewModel.isSaving) "Guardando..." else "Registrar asiento")
        }
    }
}
