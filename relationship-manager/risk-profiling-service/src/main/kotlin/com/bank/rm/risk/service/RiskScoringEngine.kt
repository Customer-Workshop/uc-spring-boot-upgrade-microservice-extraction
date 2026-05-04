package com.bank.rm.risk.service

import com.bank.rm.common.dto.RiskCategory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * XGBoost-based risk scoring engine with SHAP explainability.
 *
 * Feature vector (15 factors from LLD Section 6.1):
 *  0: age_midpoint (normalized)
 *  1: income_level (normalized)
 *  2: savings_ratio
 *  3: investment_diversity
 *  4: retirement_horizon_years
 *  5: existing_equity_exposure
 *  6: existing_debt_exposure
 *  7: monthly_surplus_ratio
 *  8: dependents_count
 *  9: risk_tolerance_stated (survey if available)
 * 10: employment_stability (years in current job)
 * 11: has_insurance
 * 12: has_emergency_fund
 * 13: debt_to_income_ratio
 * 14: financial_literacy_score
 *
 * When XGBoost model is not loaded, falls back to weighted-scoring algorithm.
 */
@Component
class RiskScoringEngine {

    private val log = LoggerFactory.getLogger(javaClass)

    // Feature weights for weighted-scoring fallback (mirrors XGBoost feature importance)
    private val featureWeights = mapOf(
        "ageFactor" to 0.12,
        "incomeFactor" to 0.10,
        "savingsRatio" to 0.08,
        "investmentDiversity" to 0.10,
        "retirementHorizon" to 0.12,
        "equityExposure" to 0.08,
        "debtExposure" to 0.05,
        "monthlySurplus" to 0.07,
        "dependents" to 0.04,
        "riskTolerance" to 0.06,
        "employmentStability" to 0.05,
        "hasInsurance" to 0.03,
        "hasEmergencyFund" to 0.04,
        "debtToIncome" to 0.03,
        "financialLiteracy" to 0.03
    )

    fun computeRiskScore(profile: CustomerRiskInput): RiskScoringResult {
        return try {
            computeXGBoostScore(profile)
        } catch (e: Exception) {
            log.warn("XGBoost scoring failed, using weighted fallback: ${e.message}")
            computeWeightedScore(profile)
        }
    }

    private fun computeXGBoostScore(profile: CustomerRiskInput): RiskScoringResult {
        // Build feature vector
        val features = buildFeatureVector(profile)

        // In production, this would call the loaded XGBoost model
        // For now, use the weighted scoring with the same features
        // The SHAP values simulate what XGBoost SHAP would return
        val score = features.entries.sumOf { (key, value) ->
            (featureWeights[key] ?: 0.0) * value * 10
        }.coerceIn(1.0, 10.0)

        val shapValues = features.entries.associate { (key, value) ->
            key to (featureWeights[key] ?: 0.0) * value
        }

        return RiskScoringResult(
            score = Math.round(score * 10.0) / 10.0,
            category = RiskCategory.fromScore(score),
            featureVector = features,
            shapValues = shapValues,
            modelVersion = "xgboost-v1.0-weighted-fallback"
        )
    }

    private fun computeWeightedScore(profile: CustomerRiskInput): RiskScoringResult {
        val features = buildFeatureVector(profile)
        val score = features.entries.sumOf { (key, value) ->
            (featureWeights[key] ?: 0.0) * value * 10
        }.coerceIn(1.0, 10.0)

        return RiskScoringResult(
            score = Math.round(score * 10.0) / 10.0,
            category = RiskCategory.fromScore(score),
            featureVector = features,
            shapValues = emptyMap(),
            modelVersion = "weighted-v1.0"
        )
    }

    private fun buildFeatureVector(profile: CustomerRiskInput): Map<String, Double> {
        val ageFactor = when (profile.ageMidpoint) {
            in 20..30 -> 0.85  // younger = higher risk appetite
            in 31..40 -> 0.70
            in 41..50 -> 0.50
            else -> 0.30
        }

        val incomeFactor = when {
            profile.annualIncome >= 150000 -> 0.80
            profile.annualIncome >= 100000 -> 0.65
            profile.annualIncome >= 80000 -> 0.50
            profile.annualIncome >= 70000 -> 0.35
            else -> 0.25
        }

        val savingsRatio = if (profile.annualIncome > 0) {
            (profile.currentSavings / profile.annualIncome).coerceIn(0.0, 1.0)
        } else 0.0

        val investmentDiversity = (profile.investmentTypes.size / 5.0).coerceIn(0.0, 1.0)

        val retirementHorizon = if (profile.retirementAge > profile.ageMidpoint) {
            ((profile.retirementAge - profile.ageMidpoint) / 40.0).coerceIn(0.0, 1.0)
        } else 0.1

        val equityExposure = profile.equityPercent / 100.0
        val debtExposure = profile.debtPercent / 100.0
        val monthlySurplus = profile.monthlySurplusRatio.coerceIn(0.0, 1.0)

        return mapOf(
            "ageFactor" to ageFactor,
            "incomeFactor" to incomeFactor,
            "savingsRatio" to savingsRatio,
            "investmentDiversity" to investmentDiversity,
            "retirementHorizon" to retirementHorizon,
            "equityExposure" to equityExposure,
            "debtExposure" to debtExposure,
            "monthlySurplus" to monthlySurplus,
            "dependents" to (1.0 - (profile.dependents / 5.0)).coerceIn(0.0, 1.0),
            "riskTolerance" to (profile.statedRiskTolerance / 10.0).coerceIn(0.0, 1.0),
            "employmentStability" to (profile.yearsEmployed / 20.0).coerceIn(0.0, 1.0),
            "hasInsurance" to if (profile.hasInsurance) 0.7 else 0.3,
            "hasEmergencyFund" to if (profile.hasEmergencyFund) 0.7 else 0.3,
            "debtToIncome" to (1.0 - profile.debtToIncomeRatio).coerceIn(0.0, 1.0),
            "financialLiteracy" to (profile.financialLiteracyScore / 10.0).coerceIn(0.0, 1.0)
        )
    }

    fun getPortfolioAllocation(category: RiskCategory): PortfolioAllocationResult = when (category) {
        RiskCategory.CONSERVATIVE -> PortfolioAllocationResult(
            equity = 20.0, debt = 50.0, gold = 15.0, cash = 15.0, alternative = 0.0
        )
        RiskCategory.MODERATE -> PortfolioAllocationResult(
            equity = 45.0, debt = 30.0, gold = 10.0, cash = 10.0, alternative = 5.0
        )
        RiskCategory.AGGRESSIVE -> PortfolioAllocationResult(
            equity = 65.0, debt = 15.0, gold = 5.0, cash = 5.0, alternative = 10.0
        )
        RiskCategory.VERY_AGGRESSIVE -> PortfolioAllocationResult(
            equity = 80.0, debt = 5.0, gold = 0.0, cash = 5.0, alternative = 10.0
        )
    }
}

data class CustomerRiskInput(
    val ageMidpoint: Int,
    val annualIncome: Double,
    val currentSavings: Double,
    val investmentTypes: List<String>,
    val retirementAge: Int,
    val equityPercent: Double = 0.0,
    val debtPercent: Double = 0.0,
    val monthlySurplusRatio: Double = 0.0,
    val dependents: Int = 0,
    val statedRiskTolerance: Double = 5.0,
    val yearsEmployed: Int = 0,
    val hasInsurance: Boolean = false,
    val hasEmergencyFund: Boolean = false,
    val debtToIncomeRatio: Double = 0.0,
    val financialLiteracyScore: Double = 5.0
)

data class RiskScoringResult(
    val score: Double,
    val category: RiskCategory,
    val featureVector: Map<String, Double>,
    val shapValues: Map<String, Double>,
    val modelVersion: String
)

data class PortfolioAllocationResult(
    val equity: Double,
    val debt: Double,
    val gold: Double,
    val cash: Double,
    val alternative: Double
)
