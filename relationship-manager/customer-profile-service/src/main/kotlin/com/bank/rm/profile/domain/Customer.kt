package com.bank.rm.profile.domain

import com.bank.rm.common.dto.AgeGroup
import com.bank.rm.common.dto.CommunicationChannel
import com.bank.rm.common.dto.RiskCategory
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "customers")
data class Customer(
    @Id
    val id: UUID = UUID.randomUUID(),

    val externalId: String? = null,

    @Column(nullable = false)
    var name: String,

    @Column(unique = true)
    var email: String? = null,

    var phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var ageGroup: AgeGroup,

    var location: String? = null,

    @Column(nullable = false)
    var status: String = "ACTIVE",

    val isAnonymous: Boolean = false,
    val anonymousSessionId: UUID? = null,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "financial_profiles")
data class FinancialProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false)
    var incomeSource: String, // SALARIED, BUSINESS, etc.

    @Column(nullable = false)
    var incomeRange: String, // 60-70K, 70-80K, etc.

    @Column(columnDefinition = "jsonb")
    var currentInvestments: String = "{}",

    var currentSavings: BigDecimal = BigDecimal.ZERO,
    var monthlyExpenses: BigDecimal? = null,
    var monthlySavingsCapacity: BigDecimal? = null,
    var retirementTargetAmount: BigDecimal? = null,
    var retirementTargetAge: Int? = null,
    var currency: String = "INR",

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "risk_profiles")
data class RiskProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    val riskScore: Double,

    @Enumerated(EnumType.STRING)
    val riskCategory: RiskCategory,

    @Column(columnDefinition = "jsonb")
    val assessmentData: String = "{}",

    val modelVersion: String,
    val explanation: String? = null,
    val assessedAt: Instant = Instant.now(),
    val expiresAt: Instant? = null,
    val isCurrent: Boolean = true,

    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "communication_preferences")
data class CommunicationPreference(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    var preferredChannel: CommunicationChannel,

    var preferredTimeStart: LocalTime? = null,
    var preferredTimeEnd: LocalTime? = null,
    var preferredDays: String? = null,
    var timezone: String = "Asia/Kolkata",

    var optInSms: Boolean = false,
    var optInWhatsapp: Boolean = false,
    var optInEmail: Boolean = true,
    var optInPhone: Boolean = false,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
