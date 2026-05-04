package com.bank.rm.projection.service

import com.bank.rm.common.dto.RiskCategory
import org.apache.commons.math3.distribution.NormalDistribution
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Monte Carlo simulation engine for wealth projection.
 * Runs 10,000 scenarios to model portfolio growth with stochastic returns.
 *
 * Per LLD Section 7: Uses asset-class return assumptions, volatility,
 * and correlation to simulate realistic portfolio outcomes.
 */
@Component
class MonteCarloEngine {

    private val log = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newFixedThreadPool(4)

    companion object {
        const val DEFAULT_SCENARIOS = 10_000
        const val INFLATION_RATE = 0.06 // 6% India CPI
    }

    // Asset class return assumptions (annualized)
    private val assetReturns = mapOf(
        "equity" to AssetAssumption(expectedReturn = 0.12, volatility = 0.18),
        "debt" to AssetAssumption(expectedReturn = 0.07, volatility = 0.04),
        "gold" to AssetAssumption(expectedReturn = 0.08, volatility = 0.12),
        "cash" to AssetAssumption(expectedReturn = 0.04, volatility = 0.01),
        "alternative" to AssetAssumption(expectedReturn = 0.15, volatility = 0.25)
    )

    fun runProjection(input: ProjectionInput): ProjectionResult {
        log.info("Running Monte Carlo simulation: {} scenarios, {} years",
            input.scenarios, input.projectionYears)

        val allocation = input.portfolioAllocation
        val scenarios = input.scenarios

        // Run simulation in parallel batches
        val batchSize = scenarios / 4
        val futures = (0 until 4).map { batch ->
            CompletableFuture.supplyAsync({
                simulateBatch(input, batch * batchSize, batchSize)
            }, executor)
        }

        val allFinalValues = futures.flatMap { it.get() }
        val sorted = allFinalValues.sorted()

        // Calculate percentiles
        val p10 = percentile(sorted, 10)
        val p25 = percentile(sorted, 25)
        val p50 = percentile(sorted, 50)
        val p75 = percentile(sorted, 75)
        val p90 = percentile(sorted, 90)

        // Calculate year-by-year median trajectory
        val yearlyMedian = calculateYearlyMedian(input)

        // Calculate probability of reaching target
        val targetProbability = if (input.targetAmount > 0) {
            allFinalValues.count { it >= input.targetAmount }.toDouble() / scenarios * 100
        } else 0.0

        // Real (inflation-adjusted) values
        val inflationFactor = (1 + INFLATION_RATE).pow(input.projectionYears)
        val realP50 = p50 / inflationFactor

        return ProjectionResult(
            nominalPercentiles = PercentileValues(p10, p25, p50, p75, p90),
            realPercentiles = PercentileValues(
                p10 / inflationFactor, p25 / inflationFactor,
                realP50, p75 / inflationFactor, p90 / inflationFactor
            ),
            yearlyProjection = yearlyMedian,
            targetAmount = input.targetAmount,
            probabilityOfTarget = Math.round(targetProbability * 10.0) / 10.0,
            meanFinalValue = allFinalValues.average(),
            scenarios = scenarios,
            projectionYears = input.projectionYears,
            inflationRate = INFLATION_RATE
        )
    }

    private fun simulateBatch(input: ProjectionInput, startIdx: Int, count: Int): List<Double> {
        val results = mutableListOf<Double>()
        val normal = NormalDistribution()

        for (i in 0 until count) {
            var portfolio = input.initialInvestment

            for (year in 1..input.projectionYears) {
                // Generate correlated returns for each asset class
                val yearReturn = calculatePortfolioReturn(input.portfolioAllocation, normal)
                portfolio = portfolio * (1 + yearReturn) + input.annualContribution
            }

            results.add(portfolio)
        }

        return results
    }

    private fun calculatePortfolioReturn(
        allocation: Map<String, Double>,
        normal: NormalDistribution
    ): Double {
        var portfolioReturn = 0.0

        for ((assetClass, weight) in allocation) {
            val assumption = assetReturns[assetClass] ?: continue
            val randomReturn = assumption.expectedReturn + assumption.volatility * normal.sample()
            portfolioReturn += weight / 100.0 * randomReturn
        }

        return portfolioReturn
    }

    private fun calculateYearlyMedian(input: ProjectionInput): List<YearlyProjection> {
        val normal = NormalDistribution()
        val miniScenarios = 1000
        val results = Array(input.projectionYears) { mutableListOf<Double>() }

        repeat(miniScenarios) {
            var portfolio = input.initialInvestment
            for (year in 0 until input.projectionYears) {
                val yearReturn = calculatePortfolioReturn(input.portfolioAllocation, normal)
                portfolio = portfolio * (1 + yearReturn) + input.annualContribution
                results[year].add(portfolio)
            }
        }

        return results.mapIndexed { idx, values ->
            val sorted = values.sorted()
            val inflationFactor = (1 + INFLATION_RATE).pow(idx + 1)
            YearlyProjection(
                year = idx + 1,
                nominalMedian = percentile(sorted, 50),
                realMedian = percentile(sorted, 50) / inflationFactor,
                p25 = percentile(sorted, 25),
                p75 = percentile(sorted, 75)
            )
        }
    }

    private fun percentile(sorted: List<Double>, percentile: Int): Double {
        val index = (percentile / 100.0 * (sorted.size - 1)).toInt()
        return sorted[index]
    }
}

data class AssetAssumption(
    val expectedReturn: Double,
    val volatility: Double
)

data class ProjectionInput(
    val initialInvestment: Double,
    val annualContribution: Double,
    val portfolioAllocation: Map<String, Double>,
    val projectionYears: Int,
    val targetAmount: Double = 0.0,
    val scenarios: Int = MonteCarloEngine.DEFAULT_SCENARIOS
)

data class ProjectionResult(
    val nominalPercentiles: PercentileValues,
    val realPercentiles: PercentileValues,
    val yearlyProjection: List<YearlyProjection>,
    val targetAmount: Double,
    val probabilityOfTarget: Double,
    val meanFinalValue: Double,
    val scenarios: Int,
    val projectionYears: Int,
    val inflationRate: Double
)

data class PercentileValues(
    val p10: Double,
    val p25: Double,
    val p50: Double,
    val p75: Double,
    val p90: Double
)

data class YearlyProjection(
    val year: Int,
    val nominalMedian: Double,
    val realMedian: Double,
    val p25: Double,
    val p75: Double
)
