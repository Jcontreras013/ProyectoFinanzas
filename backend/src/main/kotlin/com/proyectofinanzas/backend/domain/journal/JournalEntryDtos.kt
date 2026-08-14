package com.proyectofinanzas.backend.domain.journal

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class JournalEntryLineRequest(
    @field:NotNull val accountId: UUID,
    val partyId: UUID? = null,
    @field:NotNull @field:PositiveOrZero val debit: BigDecimal = BigDecimal.ZERO,
    @field:NotNull @field:PositiveOrZero val credit: BigDecimal = BigDecimal.ZERO,
    val description: String? = null,
)

data class CreateJournalEntryRequest(
    @field:NotNull val entryDate: LocalDate,
    @field:NotBlank val description: String,
    @field:NotEmpty @field:Valid val lines: List<JournalEntryLineRequest>,
)

data class ReverseJournalEntryRequest(
    @field:NotBlank val reason: String,
)

data class JournalEntryLineResponse(
    val id: UUID,
    val lineNumber: Int,
    val accountId: UUID,
    val accountCode: String,
    val accountName: String,
    val partyId: UUID?,
    val partyName: String?,
    val debit: BigDecimal,
    val credit: BigDecimal,
    val description: String?,
)

data class JournalEntryResponse(
    val id: UUID,
    val entryNumber: Long,
    val entryDate: LocalDate,
    val description: String,
    val sourceType: JournalSourceType,
    val sourceId: UUID?,
    val reversalOfId: UUID?,
    val createdByName: String,
    val createdAt: Instant,
    val lines: List<JournalEntryLineResponse>,
)
