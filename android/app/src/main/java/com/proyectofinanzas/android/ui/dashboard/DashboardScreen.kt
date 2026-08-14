package com.proyectofinanzas.android.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectofinanzas.android.data.remote.DashboardKpisDto
import com.proyectofinanzas.android.ui.common.ErrorBox
import com.proyectofinanzas.android.ui.common.LoadingBox
import com.proyectofinanzas.android.ui.common.UiState
import com.proyectofinanzas.android.ui.common.formatMoney

private data class Kpi(val label: String, val value: String)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorBox(current.message)
        is UiState.Success -> DashboardContent(current.data)
    }
}

@Composable
private fun DashboardContent(kpis: DashboardKpisDto) {
    val kpiItems = listOf(
        Kpi("Ingresos del período", formatMoney(kpis.revenueInPeriod)),
        Kpi("Gastos del período", formatMoney(kpis.expensesInPeriod)),
        Kpi("Utilidad neta", formatMoney(kpis.netIncomeInPeriod)),
        Kpi("Caja en Lempiras", formatMoney(kpis.cashBalanceHnl, "HNL")),
        Kpi("Caja en Dólares", formatMoney(kpis.cashBalanceUsd, "USD")),
        Kpi("Cuentas por cobrar", formatMoney(kpis.accountsReceivableOutstanding)),
        Kpi("Cuentas por pagar", formatMoney(kpis.accountsPayableOutstanding)),
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel principal", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Del ${kpis.from} al ${kpis.to}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            items(kpiItems) { kpi ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            kpi.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(kpi.value, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
