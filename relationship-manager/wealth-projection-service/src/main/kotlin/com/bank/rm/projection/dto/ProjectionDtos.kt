package com.bank.rm.projection.dto

import com.bank.rm.common.dto.RiskCategory
import java.util.UUID

data class ProjectionRequest(
    val initialInvestment: Double,
    val annualContribution: Double,
    val riskCategory: RiskCategory,
    val projectionYears: Int = 20,
    val targetAmount: Double = 0.0,
    val scenarios: Int = 10000,
    val customAllocation: Map<String, Double>? = null
)

data class ProjectionResponse(
    val projectionId: UUID,
    val customerId: UUID,
    val nominalPercentiles: PercentileValuesDto,
    val realPercentiles: PercentileValuesDto,
    val yearlyProjection: List<YearlyProjectionDto>,
    val targetAmount: Double,
    val probabilityOfTarget: Double,
    val meanFinalValue: Double,
    val scenarios: Int,
    val projectionYears: Int,
    val inflationRate: Double,
    val portfolioAllocation: Map<String, Double>
)

data class PercentileValuesDto(
    val p10: Double,
    val p25: Double,
    val p50: Double,
    val p75: Double,
    val p90: Double
)

data class YearlyProjectionDto(
    val year: Int,
    val nominalMedian: Double,
    val realMedian: Double,
    val p25: Double,
    val p75: Double
)
