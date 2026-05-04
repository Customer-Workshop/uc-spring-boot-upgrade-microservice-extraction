package com.bank.rm.projection.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.projection.dto.ProjectionRequest
import com.bank.rm.projection.dto.ProjectionResponse
import com.bank.rm.projection.service.WealthProjectionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/projections")
class ProjectionController(private val projectionService: WealthProjectionService) {

    @PostMapping("/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun project(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: ProjectionRequest
    ): ApiResponse<ProjectionResponse> =
        ApiResponse(success = true, data = projectionService.project(customerId, request))

    @GetMapping("/{customerId}/latest")
    fun getLatest(@PathVariable customerId: UUID): ApiResponse<ProjectionResponse?> =
        ApiResponse(success = true, data = projectionService.getLatestProjection(customerId))
}
