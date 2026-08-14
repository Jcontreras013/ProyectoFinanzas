package com.proyectofinanzas.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.expiration-hours}") private val expirationHours: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun generateToken(userId: UUID, email: String, role: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
            .signWith(key)
            .compact()
    }

    fun parseClaims(token: String): Claims? =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (ex: JwtException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
}
