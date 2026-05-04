package com.bank.rm.product.controller

import com.bank.rm.common.dto.*
import com.bank.rm.product.dto.*
import com.bank.rm.product.service.ProductCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductCatalogService) {

    @GetMapping
    fun getAllProducts(): ApiResponse<List<ProductResponse>> =
        ApiResponse(success = true, data = productService.getAllProducts())

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: UUID): ApiResponse<ProductResponse> =
        ApiResponse(success = true, data = productService.getProduct(productId))

    @GetMapping("/category/{category}")
    fun getByCategory(@PathVariable category: ProductCategory): ApiResponse<List<ProductResponse>> =
        ApiResponse(success = true, data = productService.getProductsByCategory(category))

    @GetMapping("/risk-category/{riskCategory}")
    fun getForRiskCategory(@PathVariable riskCategory: RiskCategory): ApiResponse<List<ProductWithAllocationResponse>> =
        ApiResponse(success = true, data = productService.getProductsForRiskCategory(riskCategory))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ApiResponse<ProductResponse> =
        ApiResponse(success = true, data = productService.createProduct(request))

    @PatchMapping("/{productId}")
    fun updateProduct(@PathVariable productId: UUID, @Valid @RequestBody request: UpdateProductRequest): ApiResponse<ProductResponse> =
        ApiResponse(success = true, data = productService.updateProduct(productId, request))
}
