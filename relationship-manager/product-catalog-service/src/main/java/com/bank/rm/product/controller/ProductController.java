package com.bank.rm.product.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.product.domain.Product;
import com.bank.rm.product.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.ok(productRepository.findByActiveTrue()));
    }
}
