package com.proyectofinanzas.backend.domain.journal

import com.proyectofinanzas.backend.common.BusinessRuleException
import com.proyectofinanzas.backend.domain.account.AccountRepository
import com.proyectofinanzas.backend.domain.audit.AuditService
import com.proyectofinanzas.backend.domain.party.PartyRepository
import com.proyectofinanzas.backend.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Valida las reglas de negocio de PostingService que se evalúan ANTES de tocar la base de
 * datos (desbalance, monto cero, línea con débito y crédito simultáneos), por lo que se
 * pueden probar sin persistencia real. La ruta feliz completa (con inserciones) se verificó
 * manualmente end-to-end contra Postgres; una prueba de integración con Testcontainers queda
 * pendiente para un entorno con Docker disponible.
 */
class PostingServiceValidationTest {

    private val postingService = PostingService(
        journalEntryRepository = mock(JournalEntryRepository::class.java),
        journalEntryLineRepository = mock(JournalEntryLineRepository::class.java),
        accountRepository = mock(AccountRepository::class.java),
        partyRepository = mock(PartyRepository::class.java),
        userRepository = mock(UserRepository::class.java),
        auditService = mock(AuditService::class.java),
    )

    @Test
    fun `rechaza un asiento con debitos y creditos que no cuadran`() {
        val lines = listOf(
            PostingLine(accountId = UUID.randomUUID(), debit = BigDecimal("100.00")),
            PostingLine(accountId = UUID.randomUUID(), credit = BigDecimal("99.99")),
        )
        assertThrows(BusinessRuleException::class.java) {
            postingService.post(
                entryDate = LocalDate.now(),
                description = "desbalanceado",
                sourceType = JournalSourceType.MANUAL,
                sourceId = null,
                lines = lines,
                createdById = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `rechaza un asiento con monto total en cero`() {
        val lines = listOf(
            PostingLine(accountId = UUID.randomUUID(), debit = BigDecimal.ZERO),
            PostingLine(accountId = UUID.randomUUID(), credit = BigDecimal.ZERO),
        )
        assertThrows(BusinessRuleException::class.java) {
            postingService.post(
                entryDate = LocalDate.now(),
                description = "monto cero",
                sourceType = JournalSourceType.MANUAL,
                sourceId = null,
                lines = lines,
                createdById = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `rechaza un asiento con menos de dos lineas`() {
        val lines = listOf(PostingLine(accountId = UUID.randomUUID(), debit = BigDecimal.TEN))
        assertThrows(IllegalArgumentException::class.java) {
            postingService.post(
                entryDate = LocalDate.now(),
                description = "una sola linea",
                sourceType = JournalSourceType.MANUAL,
                sourceId = null,
                lines = lines,
                createdById = UUID.randomUUID(),
            )
        }
    }
}
