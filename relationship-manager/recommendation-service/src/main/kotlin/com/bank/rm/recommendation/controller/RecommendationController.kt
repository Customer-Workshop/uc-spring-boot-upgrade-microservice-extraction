package com.bank.rm.recommendation.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.recommendation.dto.*
import com.bank.rm.recommendation.service.RecommendationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/recommendations")
class RecommendationController(private val recommendationService: RecommendationService) {

    @PostMapping("/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun generate(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: RecommendationRequest
    ): ApiResponse<RecommendationResponse> =
        ApiResponse(success = true, data = recommendationService.generateRecommendations(customerId, request))

    @GetMapping("/{customerId}/latest")
    fun getLatest(@PathVariable customerId: UUID): ApiResponse<RecommendationResponse?> =
        ApiResponse(success = true, data = recommendationService.getLatestRecommendation(customerId))
}
