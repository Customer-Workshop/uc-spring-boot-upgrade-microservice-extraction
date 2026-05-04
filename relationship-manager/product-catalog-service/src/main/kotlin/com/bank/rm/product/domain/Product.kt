package com.bank.rm.product.domain

import com.bank.rm.common.dto.ProductCategory
import com.bank.rm.common.dto.ProductRiskLevel
import com.bank.rm.common.dto.RiskCategory
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "products")
data class Product(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ProductCategory,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val riskLevel: ProductRiskLevel,

    val expectedReturnMin: BigDecimal? = null,
    val expectedReturnMax: BigDecimal? = null,
    val minInvestment: BigDecimal = BigDecimal.ZERO,
    val maxInvestment: BigDecimal? = null,
    val tenureMinMonths: Int? = null,
    val tenureMaxMonths: Int? = null,
    val taxBenefitSection: String? = null,

    @Column(columnDefinition = "jsonb")
    val suitableFor: String = "[]",

    @Column(columnDefinition = "jsonb")
    val features: String = "{}",

    val isActive: Boolean = true,
    val priority: Int = 0,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "product_risk_mappings")
data class ProductRiskMapping(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val productId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val riskCategory: RiskCategory,

    val allocationPercent: Double,
    val priority: Int = 0
)
