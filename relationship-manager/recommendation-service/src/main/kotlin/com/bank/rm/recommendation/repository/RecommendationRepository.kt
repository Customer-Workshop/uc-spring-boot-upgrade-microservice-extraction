package com.bank.rm.recommendation.repository

import com.bank.rm.recommendation.domain.Recommendation
import com.bank.rm.recommendation.domain.RecommendedProduct
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecommendationRepository : JpaRepository<Recommendation, UUID> {
    fun findFirstByCustomerIdOrderByCreatedAtDesc(customerId: UUID): Recommendation?
}

interface RecommendedProductRepository : JpaRepository<RecommendedProduct, UUID> {
    fun findByRecommendationIdOrderByRankAsc(recommendationId: UUID): List<RecommendedProduct>
}
