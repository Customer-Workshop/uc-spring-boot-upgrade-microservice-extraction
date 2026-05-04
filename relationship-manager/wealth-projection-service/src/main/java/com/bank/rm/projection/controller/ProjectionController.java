package com.bank.rm.projection.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.projection.dto.ProjectionDtos.*;
import com.bank.rm.projection.service.WealthProjectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projections")
public class ProjectionController {
    private final WealthProjectionService projectionService;

    public ProjectionController(WealthProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<ProjectionResponse>> project(
            @PathVariable UUID customerId, @RequestBody ProjectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(projectionService.project(customerId, request)));
    }
}
