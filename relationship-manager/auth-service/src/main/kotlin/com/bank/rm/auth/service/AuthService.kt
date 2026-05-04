package com.bank.rm.auth.service

import com.bank.rm.auth.domain.User
import com.bank.rm.auth.domain.UserRole
import com.bank.rm.auth.dto.*
import com.bank.rm.auth.repository.UserRepository
import com.bank.rm.common.exception.AuthenticationException
import com.bank.rm.common.exception.DuplicateResourceException
import com.bank.rm.common.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email already registered: ${request.email}")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            phone = request.phone,
            role = UserRole.CUSTOMER,
            anonymousSessionId = request.anonymousSessionId
        )
        userRepository.save(user)

        val token = jwtUtil.generateToken(user.id, listOf(user.role.name))
        val refreshToken = jwtUtil.generateRefreshToken(user.id)

        return AuthResponse(
            userId = user.id,
            token = token,
            refreshToken = refreshToken,
            profileMigrated = request.anonymousSessionId != null
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw AuthenticationException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw AuthenticationException("Invalid credentials")
        }

        if (!user.isActive) {
            throw AuthenticationException("Account is disabled")
        }

        userRepository.save(user.copy(lastLoginAt = Instant.now()))

        val token = jwtUtil.generateToken(user.id, listOf(user.role.name))
        val refreshToken = jwtUtil.generateRefreshToken(user.id)

        return AuthResponse(
            userId = user.id,
            token = token,
            refreshToken = refreshToken
        )
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val userId = jwtUtil.getUserIdFromToken(request.refreshToken)
            ?: throw AuthenticationException("Invalid refresh token")

        val user = userRepository.findById(userId).orElseThrow {
            AuthenticationException("User not found")
        }

        val token = jwtUtil.generateToken(user.id, listOf(user.role.name))
        val refreshToken = jwtUtil.generateRefreshToken(user.id)

        return TokenResponse(token, refreshToken)
    }
}
