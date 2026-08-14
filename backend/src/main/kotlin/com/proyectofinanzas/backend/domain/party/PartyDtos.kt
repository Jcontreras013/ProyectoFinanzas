package com.proyectofinanzas.backend.domain.party

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class PartyRequest(
    @field:NotNull val type: PartyType,
    @field:NotBlank val name: String,
    val rtn: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isActive: Boolean = true,
)

data class PartyResponse(
    val id: UUID,
    val type: PartyType,
    val name: String,
    val rtn: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val isActive: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(party: Party) = PartyResponse(
            id = requireNotNull(party.id),
            type = party.type,
            name = party.name,
            rtn = party.rtn,
            email = party.email,
            phone = party.phone,
            address = party.address,
            isActive = party.isActive,
            createdAt = requireNotNull(party.createdAt),
        )
    }
}
