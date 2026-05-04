package com.bank.rm.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128)
    val password: String,
    val phone: String? = null,
    val anonymousSessionId: UUID? = null
)

data class LoginRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String
)

data class AuthResponse(
    val userId: UUID,
    val token: String,
    val refreshToken: String,
    val profileMigrated: Boolean = false,
    val conversationsMigrated: Int = 0
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String
)

data class TokenResponse(
    val token: String,
    val refreshToken: String
)
