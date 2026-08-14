package com.proyectofinanzas.android.ui.invoices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectofinanzas.android.ui.common.ErrorBox
import com.proyectofinanzas.android.ui.common.LoadingBox
import com.proyectofinanzas.android.ui.common.StatusChip
import com.proyectofinanzas.android.ui.common.UiState
import com.proyectofinanzas.android.ui.common.formatMoney

@Composable
fun InvoiceDetailScreen(invoiceId: String, viewModel: InvoiceDetailViewModel = viewModel()) {
    LaunchedEffect(invoiceId) { viewModel.load(invoiceId) }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val current = state) {
            is UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Success -> {
                val invoice = current.data
                Text("Factura #${invoice.invoiceNumber}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${invoice.partyName} · Emitida ${invoice.issueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusChip(invoice.status, modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Total: ${formatMoney(invoice.total, invoice.currency)}", modifier = Modifier.weight(1f))
                    Text("Saldo: ${formatMoney(invoice.balanceInBase)}", modifier = Modifier.weight(1f))
                }

                Text("Líneas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(invoice.lines) { line ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(line.description, fontWeight = FontWeight.Medium)
                            Text(
                                "${line.quantity} x ${formatMoney(line.unitPrice, invoice.currency)} = ${formatMoney(line.lineTotal, invoice.currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
