package com.bank.rm.risk.service

import dev.langchain4j.model.chat.ChatLanguageModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ShapExplanationService(
    private val chatModel: ChatLanguageModel
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun generateExplanation(
        shapValues: Map<String, Double>,
        riskScore: Double,
        riskCategory: String
    ): String {
        return try {
            generateLLMExplanation(shapValues, riskScore, riskCategory)
        } catch (e: Exception) {
            log.warn("LLM explanation failed, using template fallback: ${e.message}")
            generateTemplateExplanation(shapValues, riskScore, riskCategory)
        }
    }

    private fun generateLLMExplanation(
        shapValues: Map<String, Double>,
        riskScore: Double,
        riskCategory: String
    ): String {
        val topFactors = shapValues.entries
            .sortedByDescending { Math.abs(it.value) }
            .take(5)
            .joinToString("\n") { "- ${formatFactorName(it.key)}: ${formatShapValue(it.value)}" }

        val prompt = """You are a financial advisor explaining a risk assessment to a customer.
Risk Score: $riskScore/10 ($riskCategory)

Top contributing factors (SHAP values, positive = increases risk appetite):
$topFactors

Write a clear, 3-4 sentence explanation of this risk profile in plain language.
Be encouraging and constructive. Don't use technical jargon.
Focus on what this means for their investment strategy."""

        return chatModel.generate(prompt)
    }

    private fun generateTemplateExplanation(
        shapValues: Map<String, Double>,
        riskScore: Double,
        riskCategory: String
    ): String {
        val topPositive = shapValues.entries.filter { it.value > 0 }
            .sortedByDescending { it.value }.take(3)
        val topNegative = shapValues.entries.filter { it.value < 0 }
            .sortedBy { it.value }.take(2)

        return buildString {
            append("Your risk profile is $riskCategory (score: $riskScore/10). ")

            if (topPositive.isNotEmpty()) {
                append("Key factors supporting a higher risk tolerance: ")
                append(topPositive.joinToString(", ") { formatFactorName(it.key) })
                append(". ")
            }

            if (topNegative.isNotEmpty()) {
                append("Factors suggesting a more cautious approach: ")
                append(topNegative.joinToString(", ") { formatFactorName(it.key) })
                append(". ")
            }

            append("This profile is well-suited for a ")
            append(when (riskCategory) {
                "CONSERVATIVE" -> "stability-focused portfolio with emphasis on fixed-income instruments."
                "MODERATE" -> "balanced portfolio mixing growth and stability."
                "AGGRESSIVE" -> "growth-oriented portfolio with significant equity exposure."
                "VERY_AGGRESSIVE" -> "high-growth portfolio maximizing equity and alternative investments."
                else -> "diversified portfolio tailored to your needs."
            })
        }
    }

    private fun formatFactorName(key: String): String = when (key) {
        "ageFactor" -> "your age and time horizon"
        "incomeFactor" -> "your income level"
        "savingsRatio" -> "your savings rate"
        "investmentDiversity" -> "your investment diversity"
        "retirementHorizon" -> "years until retirement"
        "equityExposure" -> "current equity investments"
        "debtExposure" -> "current debt instruments"
        "monthlySurplus" -> "monthly savings capacity"
        "dependents" -> "number of dependents"
        "riskTolerance" -> "your stated risk preference"
        "employmentStability" -> "employment stability"
        "hasInsurance" -> "insurance coverage"
        "hasEmergencyFund" -> "emergency fund status"
        "debtToIncome" -> "debt-to-income ratio"
        "financialLiteracy" -> "financial knowledge"
        else -> key.replace(Regex("([A-Z])"), " $1").trim().lowercase()
    }

    private fun formatShapValue(value: Double): String = when {
        value > 0.05 -> "strongly increases risk appetite"
        value > 0.02 -> "increases risk appetite"
        value > 0 -> "slightly increases risk appetite"
        value < -0.05 -> "strongly decreases risk appetite"
        value < -0.02 -> "decreases risk appetite"
        value < 0 -> "slightly decreases risk appetite"
        else -> "neutral impact"
    }
}
