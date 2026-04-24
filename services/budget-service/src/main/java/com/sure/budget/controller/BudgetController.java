package com.sure.budget.controller;

import com.sure.budget.dto.BudgetDto;
import com.sure.budget.dto.CreateBudgetRequest;
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
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDto>>> getBudgets(Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getBudgets(familyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDto>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        BudgetDto budget = budgetService.createBudget(familyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(budget));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
