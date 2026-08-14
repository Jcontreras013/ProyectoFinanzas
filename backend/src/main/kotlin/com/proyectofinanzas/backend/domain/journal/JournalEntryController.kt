package com.proyectofinanzas.backend.domain.journal

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/journal-entries")
class JournalEntryController(
    private val journalEntryService: JournalEntryService,
) {
    @GetMapping
    fun list(pageable: Pageable): Page<JournalEntryResponse> = journalEntryService.list(pageable)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): JournalEntryResponse = journalEntryService.get(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun create(@Valid @RequestBody request: CreateJournalEntryRequest): JournalEntryResponse =
        journalEntryService.createManual(request)

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun reverse(@PathVariable id: UUID, @Valid @RequestBody request: ReverseJournalEntryRequest): JournalEntryResponse =
        journalEntryService.reverse(id, request)
}
