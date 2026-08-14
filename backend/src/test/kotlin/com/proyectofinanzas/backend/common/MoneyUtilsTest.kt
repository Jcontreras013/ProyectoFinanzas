package com.proyectofinanzas.backend.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyUtilsTest {

    @Test
    fun `round redondea a 4 decimales con HALF_UP`() {
        assertEquals(BigDecimal("10.1235"), MoneyUtils.round(BigDecimal("10.12345")))
        assertEquals(BigDecimal("10.1234"), MoneyUtils.round(BigDecimal("10.123449")))
    }

    @Test
    fun `toBase convierte y redondea usando la tasa de cambio`() {
        val amount = BigDecimal("100.00")
        val rate = BigDecimal("24.70")
        assertEquals(BigDecimal("2470.0000"), MoneyUtils.toBase(amount, rate))
    }

    @Test
    fun `toBase con moneda base usa tasa 1 sin alterar el monto`() {
        val amount = BigDecimal("1234.5678")
        assertEquals(BigDecimal("1234.5678"), MoneyUtils.toBase(amount, BigDecimal.ONE))
    }
}
