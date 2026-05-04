package com.bank.rm.product.dto

import com.bank.rm.common.dto.*
import java.math.BigDecimal
import java.util.UUID

data class CreateProductRequest(
    val name: String,
    val description: String? = null,
    val category: ProductCategory,
    val riskLevel: ProductRiskLevel,
    val expectedReturnMin: BigDecimal? = null,
    val expectedReturnMax: BigDecimal? = null,
    val minInvestment: BigDecimal = BigDecimal.ZERO,
    val maxInvestment: BigDecimal? = null,
    val tenureMinMonths: Int? = null,
    val tenureMaxMonths: Int? = null,
    val taxBenefitSection: String? = null,
    val suitableFor: List<String>? = null,
    val features: Map<String, String>? = null,
    val priority: Int = 0,
    val riskMappings: List<RiskMappingInput>? = null
)

data class RiskMappingInput(
    val riskCategory: RiskCategory,
    val allocationPercent: Double,
    val priority: Int = 0
)

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val expectedReturnMin: BigDecimal? = null,
    val expectedReturnMax: BigDecimal? = null,
    val isActive: Boolean? = null
)

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val category: ProductCategory,
    val riskLevel: ProductRiskLevel,
    val expectedReturnMin: BigDecimal?,
    val expectedReturnMax: BigDecimal?,
    val minInvestment: BigDecimal,
    val maxInvestment: BigDecimal?,
    val tenureMinMonths: Int?,
    val tenureMaxMonths: Int?,
    val taxBenefitSection: String?,
    val isActive: Boolean
)

data class ProductWithAllocationResponse(
    val product: ProductResponse,
    val allocationPercent: Double,
    val priority: Int
)
