package com.proyectofinanzas.backend.domain.account

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class AccountRequest(
    @field:NotBlank val code: String,
    @field:NotBlank val name: String,
    @field:NotNull val type: AccountType,
    val parentId: UUID? = null,
    val allowsPosting: Boolean = true,
    val systemRole: AccountSystemRole? = null,
    val isActive: Boolean = true,
)

data class AccountResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val type: AccountType,
    val parentId: UUID?,
    val allowsPosting: Boolean,
    val systemRole: AccountSystemRole?,
    val isActive: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(account: Account) = AccountResponse(
            id = requireNotNull(account.id),
            code = account.code,
            name = account.name,
            type = account.type,
            parentId = account.parent?.id,
            allowsPosting = account.allowsPosting,
            systemRole = account.systemRole,
            isActive = account.isActive,
            createdAt = requireNotNull(account.createdAt),
        )
    }
}
