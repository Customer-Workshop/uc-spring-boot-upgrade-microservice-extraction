package com.bank.rm.recommendation.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.recommendation.dto.RecommendationDtos.*;
import com.bank.rm.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommend(
            @PathVariable UUID customerId, @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.generateRecommendations(customerId, request)));
    }
}
