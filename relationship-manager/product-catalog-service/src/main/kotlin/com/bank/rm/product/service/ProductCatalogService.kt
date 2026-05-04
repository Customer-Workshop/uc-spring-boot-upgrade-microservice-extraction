package com.bank.rm.product.service

import com.bank.rm.common.dto.*
import com.bank.rm.common.event.DomainEvent
import com.bank.rm.common.event.KafkaTopics
import com.bank.rm.common.event.ProductAddedEvent
import com.bank.rm.common.exception.ResourceNotFoundException
import com.bank.rm.product.domain.Product
import com.bank.rm.product.domain.ProductRiskMapping
import com.bank.rm.product.dto.*
import com.bank.rm.product.repository.ProductRepository
import com.bank.rm.product.repository.ProductRiskMappingRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProductCatalogService(
    private val productRepository: ProductRepository,
    private val riskMappingRepository: ProductRiskMappingRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val objectMapper: ObjectMapper
) {

    fun getAllProducts(): List<ProductResponse> =
        productRepository.findByIsActiveTrue().map { it.toResponse() }

    fun getProduct(productId: UUID): ProductResponse {
        val product = productRepository.findById(productId).orElseThrow {
            ResourceNotFoundException("Product not found: $productId")
        }
        return product.toResponse()
    }

    fun getProductsByCategory(category: ProductCategory): List<ProductResponse> =
        productRepository.findByCategoryAndIsActiveTrue(category).map { it.toResponse() }

    fun getProductsForRiskCategory(riskCategory: RiskCategory): List<ProductWithAllocationResponse> {
        val mappings = riskMappingRepository.findByRiskCategoryOrderByPriorityAsc(riskCategory)
        return mappings.mapNotNull { mapping ->
            productRepository.findById(mapping.productId).orElse(null)?.let { product ->
                ProductWithAllocationResponse(
                    product = product.toResponse(),
                    allocationPercent = mapping.allocationPercent,
                    priority = mapping.priority
                )
            }
        }
    }

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        val product = Product(
            name = request.name,
            description = request.description,
            category = request.category,
            riskLevel = request.riskLevel,
            expectedReturnMin = request.expectedReturnMin,
            expectedReturnMax = request.expectedReturnMax,
            minInvestment = request.minInvestment,
            maxInvestment = request.maxInvestment,
            tenureMinMonths = request.tenureMinMonths,
            tenureMaxMonths = request.tenureMaxMonths,
            taxBenefitSection = request.taxBenefitSection,
            suitableFor = objectMapper.writeValueAsString(request.suitableFor ?: emptyList<String>()),
            features = objectMapper.writeValueAsString(request.features ?: emptyMap<String, String>()),
            priority = request.priority
        )
        productRepository.save(product)

        // Create risk mappings
        request.riskMappings?.forEach { mapping ->
            riskMappingRepository.save(ProductRiskMapping(
                productId = product.id,
                riskCategory = mapping.riskCategory,
                allocationPercent = mapping.allocationPercent,
                priority = mapping.priority
            ))
        }

        kafkaTemplate.send(
            KafkaTopics.PRODUCT_EVENTS,
            product.id.toString(),
            ProductAddedEvent(productId = product.id, name = product.name, category = product.category)
        )

        return product.toResponse()
    }

    @Transactional
    fun updateProduct(productId: UUID, request: UpdateProductRequest): ProductResponse {
        val product = productRepository.findById(productId).orElseThrow {
            ResourceNotFoundException("Product not found: $productId")
        }
        val updated = product.copy(
            name = request.name ?: product.name,
            description = request.description ?: product.description,
            expectedReturnMin = request.expectedReturnMin ?: product.expectedReturnMin,
            expectedReturnMax = request.expectedReturnMax ?: product.expectedReturnMax,
            isActive = request.isActive ?: product.isActive,
            updatedAt = Instant.now()
        )
        productRepository.save(updated)
        return updated.toResponse()
    }

    private fun Product.toResponse() = ProductResponse(
        id = id, name = name, description = description,
        category = category, riskLevel = riskLevel,
        expectedReturnMin = expectedReturnMin, expectedReturnMax = expectedReturnMax,
        minInvestment = minInvestment, maxInvestment = maxInvestment,
        tenureMinMonths = tenureMinMonths, tenureMaxMonths = tenureMaxMonths,
        taxBenefitSection = taxBenefitSection, isActive = isActive
    )
}
