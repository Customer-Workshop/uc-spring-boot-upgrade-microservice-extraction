package com.bank.rm.recommendation.domain

import com.bank.rm.common.dto.RiskCategory
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "recommendations")
data class Recommendation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    val riskCategory: RiskCategory,

    val riskScore: Double,

    @Column(columnDefinition = "jsonb")
    val customerProfile: String = "{}",

    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "recommended_products")
data class RecommendedProduct(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val recommendationId: UUID,

    val productId: UUID,
    val productName: String,
    val category: String,
    val matchScore: Double,
    val allocationPercent: Double = 0.0,
    val rationale: String? = null,
    val rank: Int,

    val createdAt: Instant = Instant.now()
)
