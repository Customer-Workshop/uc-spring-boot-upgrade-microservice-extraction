package com.bank.rm.profile.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.profile.dto.ProfileDtos.*;
import com.bank.rm.profile.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(@RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(profileService.createProfile(request)));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(customerId)));
    }

    @PutMapping("/{customerId}/financial")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateFinancial(
            @PathVariable UUID customerId, @RequestBody UpdateFinancialRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateFinancial(customerId, request)));
    }

    @PutMapping("/{customerId}/goals")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateGoals(
            @PathVariable UUID customerId, @RequestBody UpdateGoalsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateGoals(customerId, request)));
    }

    @PutMapping("/{customerId}/channel")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateChannel(
            @PathVariable UUID customerId, @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateChannel(customerId, request)));
    }
}
