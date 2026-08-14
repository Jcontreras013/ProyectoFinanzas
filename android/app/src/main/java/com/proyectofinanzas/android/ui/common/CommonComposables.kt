package com.proyectofinanzas.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorBox(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (label, color) = statusLabelAndColor(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun statusLabelAndColor(status: String): Pair<String, androidx.compose.ui.graphics.Color> {
    val success = androidx.compose.ui.graphics.Color(0xFF2E7D32)
    val warning = androidx.compose.ui.graphics.Color(0xFFB26A00)
    val destructive = MaterialTheme.colorScheme.error
    val primary = MaterialTheme.colorScheme.primary
    return when (status) {
        "ISSUED" -> "Emitida" to primary
        "POSTED" -> "Contabilizado" to primary
        "PARTIALLY_PAID" -> "Parcialmente pagado" to warning
        "PAID" -> "Pagado" to success
        "CANCELLED" -> "Cancelado" to destructive
        else -> status to primary
    }
}
