package com.bank.rm.auth.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(unique = true)
    val phone: String? = null,

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.CUSTOMER,

    @Column(nullable = false)
    val isActive: Boolean = true,

    val anonymousSessionId: UUID? = null,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null
)

enum class UserRole {
    CUSTOMER, RELATIONSHIP_MANAGER, ADMIN
}
