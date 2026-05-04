package com.bank.rm.profile.dto

import com.bank.rm.common.dto.*
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.util.UUID

data class CreateCustomerRequest(
    @field:NotBlank val name: String,
    val email: String? = null,
    val phone: String? = null,
    val ageGroup: AgeGroup,
    val location: String? = null,
    val isAnonymous: Boolean = false,
    val anonymousSessionId: UUID? = null
)

data class UpdateCustomerRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val ageGroup: AgeGroup? = null,
    val location: String? = null
)

data class CustomerResponse(
    val id: UUID,
    val name: String,
    val email: String?,
    val phone: String?,
    val ageGroup: AgeGroup,
    val location: String?,
    val status: String
)

data class FinancialProfileRequest(
    val incomeSource: IncomeSource,
    val incomeRange: IncomeRange,
    val currentInvestments: Map<String, BigDecimal>? = null,
    val currentSavings: BigDecimal? = null,
    val monthlyExpenses: BigDecimal? = null,
    val monthlySavingsCapacity: BigDecimal? = null,
    val retirementTargetAmount: BigDecimal? = null,
    val retirementTargetAge: Int? = null
)

data class FinancialProfileResponse(
    val incomeSource: String,
    val incomeRange: String,
    val currentInvestments: Map<String, BigDecimal>,
    val currentSavings: BigDecimal,
    val monthlySavingsCapacity: BigDecimal?,
    val retirementTargetAmount: BigDecimal?,
    val retirementTargetAge: Int?
)

data class RiskProfileResponse(
    val riskScore: Double,
    val riskCategory: RiskCategory,
    val explanation: String?,
    val assessedAt: String
)

data class FullProfileResponse(
    val customer: CustomerResponse,
    val financialProfile: FinancialProfileResponse?,
    val riskProfile: RiskProfileResponse?,
    val communicationPreferences: CommunicationPreferenceResponse?
)

data class CommunicationPreferenceRequest(
    val preferredChannel: CommunicationChannel,
    val preferredTimeStart: String? = null,
    val preferredTimeEnd: String? = null,
    val preferredDays: String? = null,
    val timezone: String = "Asia/Kolkata",
    val optInSms: Boolean = false,
    val optInWhatsapp: Boolean = false,
    val optInEmail: Boolean = true,
    val optInPhone: Boolean = false
)

data class CommunicationPreferenceResponse(
    val preferredChannel: CommunicationChannel,
    val preferredTimeStart: String?,
    val preferredTimeEnd: String?,
    val preferredDays: String?,
    val timezone: String
)
