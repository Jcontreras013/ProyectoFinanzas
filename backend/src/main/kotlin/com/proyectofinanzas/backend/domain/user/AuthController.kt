package com.proyectofinanzas.backend.domain.user

import com.proyectofinanzas.backend.common.NotFoundException
import com.proyectofinanzas.backend.security.JwtService
import com.proyectofinanzas.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        val user = userRepository.findByEmailIgnoreCase(request.email)
            .orElseThrow { NotFoundException("Usuario no encontrado") }
        val token = jwtService.generateToken(requireNotNull(user.id), user.email, user.role.name)
        return LoginResponse(token = token, user = UserResponse.from(user))
    }

    @GetMapping("/me")
    fun me(): UserResponse {
        val user = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow { NotFoundException("Usuario no encontrado") }
        return UserResponse.from(user)
    }
}
