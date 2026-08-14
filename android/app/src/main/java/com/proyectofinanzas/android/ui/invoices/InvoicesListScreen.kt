package com.proyectofinanzas.android.ui.invoices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
fun InvoicesListScreen(onOpenInvoice: (String) -> Unit, viewModel: InvoicesListViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Facturas", style = MaterialTheme.typography.headlineSmall)

        when (val current = state) {
            is UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = 12.dp),
            ) {
                items(current.data) { invoice ->
                    ListItem(
                        modifier = Modifier.clickable { onOpenInvoice(invoice.id) },
                        headlineContent = { Text(invoice.partyName) },
                        overlineContent = { Text("#${invoice.invoiceNumber} · ${invoice.issueDate}") },
                        supportingContent = { Text("Total: ${formatMoney(invoice.total, invoice.currency)}") },
                        trailingContent = { StatusChip(invoice.status) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
