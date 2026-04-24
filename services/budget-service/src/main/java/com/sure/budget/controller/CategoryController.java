package com.sure.budget.controller;

import com.sure.budget.dto.CategoryDto;
import com.sure.budget.dto.CreateCategoryRequest;
import com.sure.budget.service.BudgetService;
import com.sure.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final BudgetService budgetService;

    public CategoryController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getCategories(familyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        CategoryDto category = budgetService.createCategory(familyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        budgetService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
