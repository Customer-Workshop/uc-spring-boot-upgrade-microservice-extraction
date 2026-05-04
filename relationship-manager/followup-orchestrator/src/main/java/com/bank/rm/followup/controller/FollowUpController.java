package com.bank.rm.followup.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.followup.dto.FollowUpDtos.*;
import com.bank.rm.followup.service.FollowUpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/followups")
public class FollowUpController {
    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@RequestBody CreateScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(followUpService.createSchedule(request)));
    }
}
