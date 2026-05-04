package com.bank.rm.risk.service

import com.bank.rm.common.event.DomainEvent
import com.bank.rm.common.event.KafkaTopics
import com.bank.rm.common.event.RiskAssessedEvent
import com.bank.rm.risk.domain.PortfolioAllocation
import com.bank.rm.risk.domain.RiskAssessment
import com.bank.rm.risk.dto.*
import com.bank.rm.risk.repository.PortfolioAllocationRepository
import com.bank.rm.risk.repository.RiskAssessmentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class RiskProfilingService(
    private val riskAssessmentRepository: RiskAssessmentRepository,
    private val portfolioAllocationRepository: PortfolioAllocationRepository,
    private val scoringEngine: RiskScoringEngine,
    private val shapExplanationService: ShapExplanationService,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun assessRisk(customerId: UUID, request: RiskAssessmentRequest): RiskAssessmentResponse {
        val input = CustomerRiskInput(
            ageMidpoint = request.ageMidpoint,
            annualIncome = request.annualIncome,
            currentSavings = request.currentSavings,
            investmentTypes = request.investmentTypes,
            retirementAge = request.retirementAge,
            equityPercent = request.equityPercent,
            debtPercent = request.debtPercent,
            monthlySurplusRatio = request.monthlySurplusRatio,
            dependents = request.dependents,
            statedRiskTolerance = request.statedRiskTolerance,
            yearsEmployed = request.yearsEmployed,
            hasInsurance = request.hasInsurance,
            hasEmergencyFund = request.hasEmergencyFund,
            debtToIncomeRatio = request.debtToIncomeRatio,
            financialLiteracyScore = request.financialLiteracyScore
        )

        val result = scoringEngine.computeRiskScore(input)

        // Generate SHAP explanation via LLM
        val explanation = shapExplanationService.generateExplanation(
            result.shapValues, result.score, result.category.name
        )

        // Mark previous assessments as non-current
        val previousAssessment = riskAssessmentRepository.findByCustomerIdAndIsCurrentTrue(customerId)
        if (previousAssessment != null) {
            riskAssessmentRepository.save(previousAssessment.copy(isCurrent = false))
        }

        // Save new assessment
        val assessment = RiskAssessment(
            customerId = customerId,
            riskScore = result.score,
            riskCategory = result.category,
            featureVector = objectMapper.writeValueAsString(result.featureVector),
            shapValues = objectMapper.writeValueAsString(result.shapValues),
            explanation = explanation,
            modelVersion = result.modelVersion,
            expiresAt = Instant.now().plus(90, ChronoUnit.DAYS)
        )
        riskAssessmentRepository.save(assessment)

        // Generate and save portfolio allocation
        val allocation = scoringEngine.getPortfolioAllocation(result.category)
        portfolioAllocationRepository.save(PortfolioAllocation(
            assessmentId = assessment.id,
            customerId = customerId,
            riskCategory = result.category,
            equityPercent = allocation.equity,
            debtPercent = allocation.debt,
            goldPercent = allocation.gold,
            cashPercent = allocation.cash,
            alternativePercent = allocation.alternative
        ))

        // Publish event
        kafkaTemplate.send(
            KafkaTopics.RISK_EVENTS,
            customerId.toString(),
            RiskAssessedEvent(
                customerId = customerId,
                riskScore = result.score,
                riskCategory = result.category,
                previousCategory = previousAssessment?.riskCategory
            )
        )

        return RiskAssessmentResponse(
            assessmentId = assessment.id,
            customerId = customerId,
            riskScore = result.score,
            riskCategory = result.category.name,
            explanation = explanation,
            topFactors = result.shapValues.entries
                .sortedByDescending { Math.abs(it.value) }
                .take(5)
                .map { FactorContribution(it.key, it.value) },
            portfolioAllocation = PortfolioAllocationResponse(
                allocation.equity, allocation.debt, allocation.gold, allocation.cash, allocation.alternative
            ),
            modelVersion = result.modelVersion,
            assessedAt = assessment.assessedAt.toString()
        )
    }

    fun getLatestAssessment(customerId: UUID): RiskAssessmentResponse? {
        val assessment = riskAssessmentRepository.findByCustomerIdAndIsCurrentTrue(customerId) ?: return null
        val allocationEntity = portfolioAllocationRepository.findByAssessmentId(assessment.id)

        return RiskAssessmentResponse(
            assessmentId = assessment.id,
            customerId = customerId,
            riskScore = assessment.riskScore,
            riskCategory = assessment.riskCategory.name,
            explanation = assessment.explanation,
            topFactors = parseShapValues(assessment.shapValues),
            portfolioAllocation = allocationEntity?.let {
                PortfolioAllocationResponse(it.equityPercent, it.debtPercent, it.goldPercent, it.cashPercent, it.alternativePercent)
            },
            modelVersion = assessment.modelVersion,
            assessedAt = assessment.assessedAt.toString()
        )
    }

    fun getAssessmentHistory(customerId: UUID): List<RiskAssessmentResponse> =
        riskAssessmentRepository.findByCustomerIdOrderByAssessedAtDesc(customerId).map {
            RiskAssessmentResponse(
                assessmentId = it.id,
                customerId = customerId,
                riskScore = it.riskScore,
                riskCategory = it.riskCategory.name,
                explanation = it.explanation,
                topFactors = parseShapValues(it.shapValues),
                modelVersion = it.modelVersion,
                assessedAt = it.assessedAt.toString()
            )
        }

    private fun parseShapValues(json: String): List<FactorContribution> = try {
        val map: Map<String, Double> = objectMapper.readValue(
            json, objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, Double::class.java)
        )
        map.entries.sortedByDescending { Math.abs(it.value) }
            .take(5)
            .map { FactorContribution(it.key, it.value) }
    } catch (e: Exception) { emptyList() }
}
