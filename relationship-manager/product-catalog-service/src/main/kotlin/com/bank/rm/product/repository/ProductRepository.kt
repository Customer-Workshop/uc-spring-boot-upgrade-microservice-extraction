package com.bank.rm.product.repository

import com.bank.rm.common.dto.ProductCategory
import com.bank.rm.common.dto.ProductRiskLevel
import com.bank.rm.common.dto.RiskCategory
import com.bank.rm.product.domain.Product
import com.bank.rm.product.domain.ProductRiskMapping
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByIsActiveTrue(): List<Product>
    fun findByCategoryAndIsActiveTrue(category: ProductCategory): List<Product>
    fun findByRiskLevelAndIsActiveTrue(riskLevel: ProductRiskLevel): List<Product>
}

interface ProductRiskMappingRepository : JpaRepository<ProductRiskMapping, UUID> {
    fun findByRiskCategoryOrderByPriorityAsc(riskCategory: RiskCategory): List<ProductRiskMapping>
    fun findByProductId(productId: UUID): List<ProductRiskMapping>
}
