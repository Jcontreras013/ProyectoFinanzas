package com.proyectofinanzas.android.ui.accounts

import androidx.compose.foundation.layout.Column
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
import com.proyectofinanzas.android.ui.common.UiState

@Composable
fun AccountsScreen(viewModel: AccountsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Plan de cuentas", style = MaterialTheme.typography.headlineSmall)

        when (val current = state) {
            is UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Success -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(current.data) { account ->
                    ListItem(
                        headlineContent = {
                            Text(
                                account.name,
                                style = if (account.allowsPosting) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                            )
                        },
                        overlineContent = { Text(account.code) },
                        supportingContent = { Text(typeLabel(account.type)) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun typeLabel(type: String): String = when (type) {
    "ASSET" -> "Activo"
    "LIABILITY" -> "Pasivo"
    "EQUITY" -> "Patrimonio"
    "INCOME" -> "Ingreso"
    "EXPENSE" -> "Gasto"
    else -> type
}
