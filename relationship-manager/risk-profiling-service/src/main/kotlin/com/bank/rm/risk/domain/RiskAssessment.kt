package com.bank.rm.risk.domain

import com.bank.rm.common.dto.RiskCategory
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "risk_assessments")
data class RiskAssessment(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    val riskScore: Double,

    @Enumerated(EnumType.STRING)
    val riskCategory: RiskCategory,

    @Column(columnDefinition = "jsonb")
    val featureVector: String = "{}",

    @Column(columnDefinition = "jsonb")
    val shapValues: String = "{}",

    val explanation: String? = null,

    val modelVersion: String = "xgboost-v1.0",
    val isCurrent: Boolean = true,
    val assessedAt: Instant = Instant.now(),
    val expiresAt: Instant? = null,

    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "portfolio_allocations")
data class PortfolioAllocation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val assessmentId: UUID,

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    val riskCategory: RiskCategory,

    val equityPercent: Double,
    val debtPercent: Double,
    val goldPercent: Double,
    val cashPercent: Double,
    val alternativePercent: Double = 0.0,

    val createdAt: Instant = Instant.now()
)
