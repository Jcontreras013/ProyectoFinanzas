package com.proyectofinanzas.backend.domain.journal

import com.proyectofinanzas.backend.common.BusinessRuleException
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.security.SecurityUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class JournalEntryService(
    private val journalEntryRepository: JournalEntryRepository,
    private val journalEntryLineRepository: JournalEntryLineRepository,
    private val postingService: PostingService,
) {

    fun createManual(request: CreateJournalEntryRequest): JournalEntryResponse {
        val lines = request.lines.map {
            PostingLine(
                accountId = it.accountId,
                partyId = it.partyId,
                debit = it.debit,
                credit = it.credit,
                description = it.description,
            )
        }
        val entry = postingService.post(
            entryDate = request.entryDate,
            description = request.description,
            sourceType = JournalSourceType.MANUAL,
            sourceId = null,
            lines = lines,
            createdById = SecurityUtils.currentUserId(),
        )
        return toResponse(entry)
    }

    fun reverse(id: UUID, request: ReverseJournalEntryRequest): JournalEntryResponse {
        val original = findEntity(id)
        if (original.sourceType == JournalSourceType.REVERSAL) {
            throw BusinessRuleException("No se puede reversar un asiento que ya es una reversión")
        }
        val reversal = postingService.reverse(
            original = original,
            entryDate = LocalDate.now(),
            reason = request.reason,
            createdById = SecurityUtils.currentUserId(),
        )
        return toResponse(reversal)
    }

    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<JournalEntryResponse> =
        journalEntryRepository.findAllByOrderByEntryDateDescEntryNumberDesc(pageable).map { toResponse(it) }

    @Transactional(readOnly = true)
    fun get(id: UUID): JournalEntryResponse = toResponse(findEntity(id))

    private fun findEntity(id: UUID): JournalEntry =
        journalEntryRepository.findById(id).orElseThrow { NotFoundException("Asiento no encontrado") }

    private fun toResponse(entry: JournalEntry): JournalEntryResponse {
        val lines = journalEntryLineRepository.findByJournalEntryIdOrderByLineNumberAsc(requireNotNull(entry.id))
        return JournalEntryResponse(
            id = requireNotNull(entry.id),
            entryNumber = entry.entryNumber,
            entryDate = entry.entryDate,
            description = entry.description,
            sourceType = entry.sourceType,
            sourceId = entry.sourceId,
            reversalOfId = entry.reversalOf?.id,
            createdByName = entry.createdBy.fullName,
            createdAt = requireNotNull(entry.createdAt),
            lines = lines.map {
                JournalEntryLineResponse(
                    id = requireNotNull(it.id),
                    lineNumber = it.lineNumber,
                    accountId = requireNotNull(it.account.id),
                    accountCode = it.account.code,
                    accountName = it.account.name,
                    partyId = it.party?.id,
                    partyName = it.party?.name,
                    debit = it.debit,
                    credit = it.credit,
                    description = it.description,
                )
            },
        )
    }
}
