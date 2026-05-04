package com.bank.rm.projection.domain

import com.bank.rm.common.dto.RiskCategory
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "wealth_projections")
data class WealthProjection(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    val riskCategory: RiskCategory,

    val initialInvestment: Double,
    val annualContribution: Double,
    val projectionYears: Int,
    val targetAmount: Double,
    val nominalP50: Double,
    val realP50: Double,
    val probabilityOfTarget: Double,
    val scenariosRun: Int,

    @Column(columnDefinition = "jsonb")
    val resultData: String,

    val createdAt: Instant = Instant.now()
)
