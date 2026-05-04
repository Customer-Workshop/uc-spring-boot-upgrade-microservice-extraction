package com.bank.rm.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

class JwtUtil(private val secret: String, private val expirationMs: Long = 3600000) {

    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: UUID, roles: List<String> = emptyList()): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("roles", roles)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    fun generateRefreshToken(userId: UUID): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs * 24))
            .signWith(key)
            .compact()

    fun validateToken(token: String): Claims? =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (e: Exception) {
            null
        }

    fun getUserIdFromToken(token: String): UUID? =
        validateToken(token)?.subject?.let { UUID.fromString(it) }
}
