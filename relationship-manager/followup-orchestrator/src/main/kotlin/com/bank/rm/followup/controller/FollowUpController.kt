package com.bank.rm.followup.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.followup.dto.*
import com.bank.rm.followup.service.FollowUpService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/followups")
class FollowUpController(private val followUpService: FollowUpService) {

    @PostMapping("/{customerId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSchedule(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ApiResponse<ScheduleResponse> =
        ApiResponse(success = true, data = followUpService.createSchedule(customerId, request))

    @GetMapping("/{customerId}/schedule")
    fun getSchedule(@PathVariable customerId: UUID): ApiResponse<ScheduleResponse?> =
        ApiResponse(success = true, data = followUpService.getSchedule(customerId))

    @GetMapping("/{customerId}/upcoming")
    fun getUpcoming(@PathVariable customerId: UUID): ApiResponse<List<FollowUpInstanceResponse>> =
        ApiResponse(success = true, data = followUpService.getUpcomingInstances(customerId))

    @PostMapping("/instances/{instanceId}/complete")
    fun complete(
        @PathVariable instanceId: UUID,
        @RequestParam conversationId: UUID?
    ): ApiResponse<FollowUpInstanceResponse> =
        ApiResponse(success = true, data = followUpService.completeFollowUp(instanceId, conversationId))
}
