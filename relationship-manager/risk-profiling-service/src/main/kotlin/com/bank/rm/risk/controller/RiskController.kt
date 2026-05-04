package com.bank.rm.risk.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.risk.dto.RiskAssessmentRequest
import com.bank.rm.risk.dto.RiskAssessmentResponse
import com.bank.rm.risk.service.RiskProfilingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/risk")
class RiskController(private val riskProfilingService: RiskProfilingService) {

    @PostMapping("/assess/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun assessRisk(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: RiskAssessmentRequest
    ): ApiResponse<RiskAssessmentResponse> =
        ApiResponse(success = true, data = riskProfilingService.assessRisk(customerId, request))

    @GetMapping("/{customerId}/latest")
    fun getLatestAssessment(@PathVariable customerId: UUID): ApiResponse<RiskAssessmentResponse?> =
        ApiResponse(success = true, data = riskProfilingService.getLatestAssessment(customerId))

    @GetMapping("/{customerId}/history")
    fun getAssessmentHistory(@PathVariable customerId: UUID): ApiResponse<List<RiskAssessmentResponse>> =
        ApiResponse(success = true, data = riskProfilingService.getAssessmentHistory(customerId))
}
