package com.bank.rm.recommendation.service

import com.bank.rm.common.dto.RiskCategory
import com.bank.rm.common.event.*
import com.bank.rm.recommendation.domain.Recommendation
import com.bank.rm.recommendation.domain.RecommendedProduct
import com.bank.rm.recommendation.dto.*
import com.bank.rm.recommendation.repository.RecommendationRepository
import com.bank.rm.recommendation.repository.RecommendedProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.model.chat.ChatLanguageModel
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RecommendationService(
    private val recommendationRepository: RecommendationRepository,
    private val recommendedProductRepository: RecommendedProductRepository,
    private val chatModel: ChatLanguageModel,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun generateRecommendations(customerId: UUID, request: RecommendationRequest): RecommendationResponse {
        // Step 1: Rule-based product matching (base ranking)
        val baseProducts = matchProducts(request.riskCategory, request.customerProfile)

        // Step 2: AI re-ranking using LLM
        val rerankedProducts = try {
            reRankWithAI(baseProducts, request.customerProfile)
        } catch (e: Exception) {
            log.warn("AI re-ranking failed, using rule-based order: ${e.message}")
            baseProducts
        }

        // Step 3: Generate AI rationale for each recommendation
        val productsWithRationale = rerankedProducts.map { product ->
            val rationale = try {
                generateProductRationale(product, request.customerProfile)
            } catch (e: Exception) {
                "This product matches your ${request.riskCategory.name.lowercase()} risk profile."
            }
            product.copy(rationale = rationale)
        }

        // Save recommendation
        val recommendation = Recommendation(
            customerId = customerId,
            riskCategory = request.riskCategory,
            riskScore = request.riskScore,
            customerProfile = objectMapper.writeValueAsString(request.customerProfile)
        )
        recommendationRepository.save(recommendation)

        productsWithRationale.forEachIndexed { idx, product ->
            recommendedProductRepository.save(RecommendedProduct(
                recommendationId = recommendation.id,
                productId = product.productId,
                productName = product.name,
                category = product.category,
                matchScore = product.matchScore,
                allocationPercent = product.allocationPercent,
                rationale = product.rationale,
                rank = idx + 1
            ))
        }

        // Emit event
        kafkaTemplate.send(
            KafkaTopics.RECOMMENDATION_EVENTS,
            customerId.toString(),
            RecommendationGeneratedEvent(
                customerId = customerId,
                recommendationId = recommendation.id,
                riskCategory = request.riskCategory
            )
        )

        return RecommendationResponse(
            recommendationId = recommendation.id,
            customerId = customerId,
            riskCategory = request.riskCategory.name,
            products = productsWithRationale,
            generatedAt = recommendation.createdAt.toString()
        )
    }

    fun getLatestRecommendation(customerId: UUID): RecommendationResponse? {
        val rec = recommendationRepository.findFirstByCustomerIdOrderByCreatedAtDesc(customerId) ?: return null
        val products = recommendedProductRepository.findByRecommendationIdOrderByRankAsc(rec.id)

        return RecommendationResponse(
            recommendationId = rec.id,
            customerId = customerId,
            riskCategory = rec.riskCategory.name,
            products = products.map {
                RecommendedProductDto(
                    productId = it.productId, name = it.productName, category = it.category,
                    matchScore = it.matchScore, allocationPercent = it.allocationPercent,
                    rationale = it.rationale
                )
            },
            generatedAt = rec.createdAt.toString()
        )
    }

    private fun matchProducts(riskCategory: RiskCategory, profile: CustomerProfileDto): List<RecommendedProductDto> {
        // Rule-based product matching with scoring
        val products = getProductCatalog()

        return products
            .filter { isProductSuitable(it, riskCategory, profile) }
            .map { product ->
                val score = calculateMatchScore(product, riskCategory, profile)
                RecommendedProductDto(
                    productId = product.id,
                    name = product.name,
                    category = product.category,
                    matchScore = score,
                    allocationPercent = product.defaultAllocation.getOrDefault(riskCategory.name, 0.0)
                )
            }
            .sortedByDescending { it.matchScore }
            .take(8)
    }

    private fun reRankWithAI(products: List<RecommendedProductDto>, profile: CustomerProfileDto): List<RecommendedProductDto> {
        val productList = products.mapIndexed { idx, p ->
            "${idx + 1}. ${p.name} (${p.category}, match: ${p.matchScore}%)"
        }.joinToString("\n")

        val prompt = """Given this customer profile:
- Age group: ${profile.ageGroup}, Income: ${profile.incomeRange}
- Savings: ${profile.currentSavings}, Goals: retire with ${profile.retirementTarget}
- Risk tolerance: ${profile.riskCategory}

Re-rank these products from most to least suitable. Return ONLY the numbers in order, comma-separated:
$productList"""

        val response = chatModel.generate(prompt)
        val indices = response.trim().split(",").mapNotNull { it.trim().toIntOrNull()?.minus(1) }

        return if (indices.size == products.size) {
            indices.mapNotNull { products.getOrNull(it) }
        } else {
            products // fallback to original order
        }
    }

    private fun generateProductRationale(product: RecommendedProductDto, profile: CustomerProfileDto): String {
        val prompt = """In one sentence, explain why "${product.name}" (${product.category}) is suitable for a 
${profile.ageGroup} year old with ${profile.incomeRange} income and ${profile.riskCategory} risk profile 
who wants to retire with ${profile.retirementTarget}."""

        return chatModel.generate(prompt).trim()
    }

    private fun isProductSuitable(product: ProductInfo, riskCategory: RiskCategory, profile: CustomerProfileDto): Boolean {
        // Check risk level compatibility
        val riskCompatible = when (riskCategory) {
            RiskCategory.CONSERVATIVE -> product.riskLevel in listOf("LOW", "MODERATE")
            RiskCategory.MODERATE -> product.riskLevel in listOf("LOW", "MODERATE", "HIGH")
            RiskCategory.AGGRESSIVE -> product.riskLevel in listOf("MODERATE", "HIGH", "VERY_HIGH")
            RiskCategory.VERY_AGGRESSIVE -> product.riskLevel in listOf("HIGH", "VERY_HIGH")
        }
        return riskCompatible
    }

    private fun calculateMatchScore(product: ProductInfo, riskCategory: RiskCategory, profile: CustomerProfileDto): Double {
        var score = 50.0

        // Risk alignment
        score += when {
            product.riskLevel == riskCategory.name -> 25.0
            else -> 10.0
        }

        // Tax benefit bonus
        if (product.taxBenefit != null) score += 10.0

        // Tenure alignment with retirement horizon
        if (product.tenureYears != null && profile.retirementAge != null) {
            val yearsToRetire = profile.retirementAge - (profile.ageGroup?.substringBefore("-")?.toIntOrNull() ?: 30)
            if (product.tenureYears <= yearsToRetire) score += 15.0
        }

        return score.coerceIn(0.0, 100.0)
    }

    // In production, this would call the Product Catalog Service
    private fun getProductCatalog(): List<ProductInfo> = listOf(
        ProductInfo(UUID.randomUUID(), "High-Yield Savings Account", "SAVINGS_ACCOUNT", "LOW", null, null, mapOf("CONSERVATIVE" to 20.0, "MODERATE" to 15.0)),
        ProductInfo(UUID.randomUUID(), "Fixed Deposit - 5 Year", "FIXED_DEPOSIT", "LOW", "80C", 5, mapOf("CONSERVATIVE" to 30.0, "MODERATE" to 15.0)),
        ProductInfo(UUID.randomUUID(), "Large Cap Equity Fund", "MUTUAL_FUND", "MODERATE", null, null, mapOf("MODERATE" to 20.0, "AGGRESSIVE" to 15.0)),
        ProductInfo(UUID.randomUUID(), "Multi Cap Growth Fund", "MUTUAL_FUND", "HIGH", null, null, mapOf("AGGRESSIVE" to 25.0, "VERY_AGGRESSIVE" to 20.0)),
        ProductInfo(UUID.randomUUID(), "ELSS Tax Saver Fund", "TAX_SAVER", "MODERATE", "80C", 3, mapOf("MODERATE" to 10.0, "AGGRESSIVE" to 10.0)),
        ProductInfo(UUID.randomUUID(), "National Pension System", "PENSION", "LOW", "80CCD", null, mapOf("CONSERVATIVE" to 15.0, "MODERATE" to 10.0)),
        ProductInfo(UUID.randomUUID(), "Sovereign Gold Bond", "GOLD", "LOW", null, 8, mapOf("CONSERVATIVE" to 15.0, "MODERATE" to 10.0)),
        ProductInfo(UUID.randomUUID(), "PPF Account", "PPF", "LOW", "80C", 15, mapOf("CONSERVATIVE" to 20.0, "MODERATE" to 10.0)),
        ProductInfo(UUID.randomUUID(), "Small Cap Equity Fund", "MUTUAL_FUND", "VERY_HIGH", null, null, mapOf("AGGRESSIVE" to 15.0, "VERY_AGGRESSIVE" to 25.0)),
        ProductInfo(UUID.randomUUID(), "REITs Fund", "BOND", "HIGH", null, null, mapOf("AGGRESSIVE" to 10.0, "VERY_AGGRESSIVE" to 10.0)),
        ProductInfo(UUID.randomUUID(), "Corporate Bond Fund", "BOND", "MODERATE", null, 3, mapOf("CONSERVATIVE" to 10.0, "MODERATE" to 15.0)),
        ProductInfo(UUID.randomUUID(), "Term Insurance Plan", "INSURANCE", "LOW", "80C", null, mapOf("CONSERVATIVE" to 5.0, "MODERATE" to 5.0, "AGGRESSIVE" to 5.0))
    )
}

data class ProductInfo(
    val id: UUID,
    val name: String,
    val category: String,
    val riskLevel: String,
    val taxBenefit: String?,
    val tenureYears: Int?,
    val defaultAllocation: Map<String, Double>
)
