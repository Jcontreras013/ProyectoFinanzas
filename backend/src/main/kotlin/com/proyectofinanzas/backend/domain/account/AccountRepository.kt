package com.proyectofinanzas.backend.domain.account

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface AccountRepository : JpaRepository<Account, UUID> {
    fun findBySystemRole(systemRole: AccountSystemRole): Optional<Account>
    fun existsByCode(code: String): Boolean
    fun findAllByOrderByCodeAsc(): List<Account>
}
