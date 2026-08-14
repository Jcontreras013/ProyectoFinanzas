package com.proyectofinanzas.backend.domain.party

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PartyRepository : JpaRepository<Party, UUID> {
    fun findAllByOrderByNameAsc(): List<Party>
}
