package com.proyectofinanzas.android.ui.common

import java.text.NumberFormat
import java.util.Locale

private val hnlFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-HN")).apply {
    currency = java.util.Currency.getInstance("HNL")
}
private val usdFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-HN")).apply {
    currency = java.util.Currency.getInstance("USD")
}

/** Los montos llegan del backend como String para no perder precisión; solo se formatean aquí. */
fun formatMoney(amount: String, currency: String = "HNL"): String {
    val value = amount.toDoubleOrNull() ?: 0.0
    return if (currency == "USD") usdFormat.format(value) else hnlFormat.format(value)
}
