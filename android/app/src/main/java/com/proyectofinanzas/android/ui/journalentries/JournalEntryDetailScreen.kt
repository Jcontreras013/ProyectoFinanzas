package com.proyectofinanzas.android.ui.journalentries

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
import com.proyectofinanzas.android.ui.common.UiState
import com.proyectofinanzas.android.ui.common.formatMoney

@Composable
fun JournalEntryDetailScreen(entryId: String, viewModel: JournalEntryDetailViewModel = viewModel()) {
    LaunchedEffect(entryId) { viewModel.load(entryId) }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val current = state) {
            is UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Success -> {
                val entry = current.data
                Text("Asiento #${entry.entryNumber}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${entry.entryDate} · ${entry.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Creado por ${entry.createdByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    items(entry.lines) { line ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("${line.accountCode} ${line.accountName}", fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    if (line.debit.toDoubleOrNull() ?: 0.0 > 0) "Debe: ${formatMoney(line.debit)}" else "",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    if (line.credit.toDoubleOrNull() ?: 0.0 > 0) "Haber: ${formatMoney(line.credit)}" else "",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
