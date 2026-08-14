package com.proyectofinanzas.backend.common

import java.math.BigDecimal
import java.math.RoundingMode

/** Todos los montos monetarios se redondean a 4 decimales, HALF_UP, para casar con NUMERIC(19,4). */
object MoneyUtils {
    private const val SCALE = 4

    fun round(amount: BigDecimal): BigDecimal = amount.setScale(SCALE, RoundingMode.HALF_UP)

    fun toBase(amount: BigDecimal, exchangeRate: BigDecimal): BigDecimal = round(amount * exchangeRate)
}
