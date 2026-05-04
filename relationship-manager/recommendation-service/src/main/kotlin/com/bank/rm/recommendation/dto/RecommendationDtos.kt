package com.bank.rm.recommendation.dto

import com.bank.rm.common.dto.RiskCategory
import java.util.UUID

data class RecommendationRequest(
    val riskCategory: RiskCategory,
    val riskScore: Double,
    val customerProfile: CustomerProfileDto
)

data class CustomerProfileDto(
    val ageGroup: String? = null,
    val incomeRange: String? = null,
    val incomeSource: String? = null,
    val currentSavings: Double = 0.0,
    val currentInvestments: Map<String, Double>? = null,
    val retirementTarget: Double? = null,
    val retirementAge: Int? = null,
    val riskCategory: String? = null
)

data class RecommendationResponse(
    val recommendationId: UUID,
    val customerId: UUID,
    val riskCategory: String,
    val products: List<RecommendedProductDto>,
    val generatedAt: String
)

data class RecommendedProductDto(
    val productId: UUID,
    val name: String,
    val category: String,
    val matchScore: Double,
    val allocationPercent: Double = 0.0,
    val rationale: String? = null
)
