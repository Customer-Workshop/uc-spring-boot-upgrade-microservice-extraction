package com.bank.rm.risk.dto

import java.util.UUID

data class RiskAssessmentRequest(
    val ageMidpoint: Int,
    val annualIncome: Double,
    val currentSavings: Double,
    val investmentTypes: List<String> = emptyList(),
    val retirementAge: Int = 60,
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

data class RiskAssessmentResponse(
    val assessmentId: UUID,
    val customerId: UUID,
    val riskScore: Double,
    val riskCategory: String,
    val explanation: String?,
    val topFactors: List<FactorContribution> = emptyList(),
    val portfolioAllocation: PortfolioAllocationResponse? = null,
    val modelVersion: String,
    val assessedAt: String
)

data class FactorContribution(
    val factor: String,
    val shapValue: Double
)

data class PortfolioAllocationResponse(
    val equity: Double,
    val debt: Double,
    val gold: Double,
    val cash: Double,
    val alternative: Double
)
