package com.proyectofinanzas.backend.domain.account

import com.proyectofinanzas.backend.common.ConflictException
import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.domain.audit.AuditAction
import com.proyectofinanzas.backend.domain.audit.AuditService
import com.proyectofinanzas.backend.security.SecurityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AccountService(
    private val accountRepository: AccountRepository,
    private val auditService: AuditService,
) {
    @Transactional(readOnly = true)
    fun list(): List<AccountResponse> = accountRepository.findAllByOrderByCodeAsc().map { AccountResponse.from(it) }

    @Transactional(readOnly = true)
    fun get(id: UUID): AccountResponse = AccountResponse.from(findEntity(id))

    fun create(request: AccountRequest): AccountResponse {
        if (accountRepository.existsByCode(request.code)) {
            throw ConflictException("Ya existe una cuenta con el código ${request.code}")
        }
        val parent = request.parentId?.let { findEntity(it) }
        val account = Account(
            code = request.code,
            name = request.name,
            type = request.type,
            parent = parent,
            allowsPosting = request.allowsPosting,
            systemRole = request.systemRole,
            isActive = request.isActive,
        )
        val saved = accountRepository.save(account)
        auditService.record(
            "accounts", requireNotNull(saved.id), AuditAction.CREATE, SecurityUtils.currentUserId(),
            newValue = mapOf("code" to saved.code, "name" to saved.name, "type" to saved.type),
        )
        return AccountResponse.from(saved)
    }

    fun update(id: UUID, request: AccountRequest): AccountResponse {
        val account = findEntity(id)
        if (request.code != account.code && accountRepository.existsByCode(request.code)) {
            throw ConflictException("Ya existe una cuenta con el código ${request.code}")
        }
        account.code = request.code
        account.name = request.name
        account.type = request.type
        account.parent = request.parentId?.let { findEntity(it) }
        account.allowsPosting = request.allowsPosting
        account.systemRole = request.systemRole
        account.isActive = request.isActive
        val saved = accountRepository.save(account)
        auditService.record(
            "accounts", id, AuditAction.UPDATE, SecurityUtils.currentUserId(),
            newValue = mapOf("code" to saved.code, "name" to saved.name, "isActive" to saved.isActive),
        )
        return AccountResponse.from(saved)
    }

    fun deactivate(id: UUID) {
        val account = findEntity(id)
        account.isActive = false
        accountRepository.save(account)
        auditService.record("accounts", id, AuditAction.DELETE, SecurityUtils.currentUserId(), newValue = null)
    }

    private fun findEntity(id: UUID): Account =
        accountRepository.findById(id).orElseThrow { NotFoundException("Cuenta no encontrada") }
}
