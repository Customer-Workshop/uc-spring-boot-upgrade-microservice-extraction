package com.sure.budget.controller;

import com.sure.budget.dto.CreateTagRequest;
import com.sure.budget.dto.TagDto;
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
@RequestMapping("/api/v1/tags")
public class TagController {

    private final BudgetService budgetService;

    public TagController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDto>>> getTags(Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getTags(familyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Valid @RequestBody CreateTagRequest request, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        TagDto tag = budgetService.createTag(familyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(tag));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        budgetService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
