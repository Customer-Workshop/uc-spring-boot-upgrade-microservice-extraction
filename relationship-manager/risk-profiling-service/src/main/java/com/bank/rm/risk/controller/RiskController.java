package com.bank.rm.risk.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.risk.dto.RiskDtos.*;
import com.bank.rm.risk.service.RiskProfilingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {
    private final RiskProfilingService riskService;

    public RiskController(RiskProfilingService riskService) {
        this.riskService = riskService;
    }

    @PostMapping("/assess/{customerId}")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> assessRisk(
            @PathVariable UUID customerId, @RequestBody RiskAssessmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(riskService.assessRisk(customerId, request)));
    }
}
