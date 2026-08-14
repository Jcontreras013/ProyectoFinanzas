package com.proyectofinanzas.backend.domain.party

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/parties")
class PartyController(
    private val partyService: PartyService,
) {
    @GetMapping
    fun list(): List<PartyResponse> = partyService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): PartyResponse = partyService.get(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun create(@Valid @RequestBody request: PartyRequest): PartyResponse = partyService.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: PartyRequest): PartyResponse =
        partyService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    fun deactivate(@PathVariable id: UUID) = partyService.deactivate(id)
}
