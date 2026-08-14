package com.proyectofinanzas.backend.security

import com.proyectofinanzas.backend.domain.user.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $username") }
        return AppUserDetails(user)
    }
}
