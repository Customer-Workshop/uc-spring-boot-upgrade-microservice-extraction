package com.bank.rm.projection.service

import com.bank.rm.common.dto.RiskCategory
import com.bank.rm.projection.domain.WealthProjection
import com.bank.rm.projection.dto.*
import com.bank.rm.projection.repository.WealthProjectionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WealthProjectionService(
    private val monteCarloEngine: MonteCarloEngine,
    private val projectionRepository: WealthProjectionRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun project(customerId: UUID, request: ProjectionRequest): ProjectionResponse {
        val allocation = buildAllocation(request.riskCategory, request.customAllocation)

        val input = ProjectionInput(
            initialInvestment = request.initialInvestment,
            annualContribution = request.annualContribution,
            portfolioAllocation = allocation,
            projectionYears = request.projectionYears,
            targetAmount = request.targetAmount,
            scenarios = request.scenarios
        )

        val result = monteCarloEngine.runProjection(input)

        // Persist projection
        val projection = WealthProjection(
            customerId = customerId,
            riskCategory = request.riskCategory,
            initialInvestment = request.initialInvestment,
            annualContribution = request.annualContribution,
            projectionYears = request.projectionYears,
            targetAmount = request.targetAmount,
            nominalP50 = result.nominalPercentiles.p50,
            realP50 = result.realPercentiles.p50,
            probabilityOfTarget = result.probabilityOfTarget,
            scenariosRun = result.scenarios,
            resultData = objectMapper.writeValueAsString(result)
        )
        projectionRepository.save(projection)

        return ProjectionResponse(
            projectionId = projection.id,
            customerId = customerId,
            nominalPercentiles = result.nominalPercentiles.toDto(),
            realPercentiles = result.realPercentiles.toDto(),
            yearlyProjection = result.yearlyProjection.map { it.toDto() },
            targetAmount = result.targetAmount,
            probabilityOfTarget = result.probabilityOfTarget,
            meanFinalValue = result.meanFinalValue,
            scenarios = result.scenarios,
            projectionYears = result.projectionYears,
            inflationRate = result.inflationRate,
            portfolioAllocation = allocation
        )
    }

    fun getLatestProjection(customerId: UUID): ProjectionResponse? {
        val projection = projectionRepository.findFirstByCustomerIdOrderByCreatedAtDesc(customerId) ?: return null
        val result: ProjectionResult = objectMapper.readValue(projection.resultData, ProjectionResult::class.java)

        return ProjectionResponse(
            projectionId = projection.id,
            customerId = customerId,
            nominalPercentiles = result.nominalPercentiles.toDto(),
            realPercentiles = result.realPercentiles.toDto(),
            yearlyProjection = result.yearlyProjection.map { it.toDto() },
            targetAmount = result.targetAmount,
            probabilityOfTarget = result.probabilityOfTarget,
            meanFinalValue = result.meanFinalValue,
            scenarios = result.scenarios,
            projectionYears = result.projectionYears,
            inflationRate = result.inflationRate,
            portfolioAllocation = buildAllocation(projection.riskCategory, null)
        )
    }

    private fun buildAllocation(
        riskCategory: RiskCategory,
        custom: Map<String, Double>?
    ): Map<String, Double> {
        if (custom != null) return custom

        return when (riskCategory) {
            RiskCategory.CONSERVATIVE -> mapOf(
                "equity" to 20.0, "debt" to 50.0, "gold" to 15.0, "cash" to 15.0, "alternative" to 0.0
            )
            RiskCategory.MODERATE -> mapOf(
                "equity" to 45.0, "debt" to 30.0, "gold" to 10.0, "cash" to 10.0, "alternative" to 5.0
            )
            RiskCategory.AGGRESSIVE -> mapOf(
                "equity" to 65.0, "debt" to 15.0, "gold" to 5.0, "cash" to 5.0, "alternative" to 10.0
            )
            RiskCategory.VERY_AGGRESSIVE -> mapOf(
                "equity" to 80.0, "debt" to 5.0, "gold" to 0.0, "cash" to 5.0, "alternative" to 10.0
            )
        }
    }

    private fun PercentileValues.toDto() = PercentileValuesDto(p10, p25, p50, p75, p90)
    private fun YearlyProjection.toDto() = YearlyProjectionDto(year, nominalMedian, realMedian, p25, p75)
}
