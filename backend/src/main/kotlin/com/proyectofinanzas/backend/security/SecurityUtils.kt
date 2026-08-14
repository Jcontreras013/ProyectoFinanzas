package com.proyectofinanzas.backend.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object SecurityUtils {
    fun currentUserId(): UUID {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? String
            ?: throw IllegalStateException("No hay usuario autenticado en el contexto de seguridad")
        return UUID.fromString(principal)
    }
}
