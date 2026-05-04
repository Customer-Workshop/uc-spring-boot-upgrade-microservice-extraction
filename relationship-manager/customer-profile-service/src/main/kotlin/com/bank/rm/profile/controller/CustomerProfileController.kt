package com.bank.rm.profile.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.profile.dto.*
import com.bank.rm.profile.service.CustomerProfileService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers")
class CustomerProfileController(private val profileService: CustomerProfileService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): ApiResponse<CustomerResponse> =
        ApiResponse(success = true, data = profileService.createCustomer(request))

    @GetMapping("/{customerId}")
    fun getCustomer(@PathVariable customerId: UUID): ApiResponse<CustomerResponse> =
        ApiResponse(success = true, data = profileService.getCustomer(customerId))

    @GetMapping("/{customerId}/profile")
    fun getFullProfile(@PathVariable customerId: UUID): ApiResponse<FullProfileResponse> =
        ApiResponse(success = true, data = profileService.getFullProfile(customerId))

    @PatchMapping("/{customerId}")
    fun updateCustomer(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: UpdateCustomerRequest
    ): ApiResponse<CustomerResponse> =
        ApiResponse(success = true, data = profileService.updateCustomer(customerId, request))

    @PutMapping("/{customerId}/financial-profile")
    fun saveFinancialProfile(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: FinancialProfileRequest
    ): ApiResponse<FinancialProfileResponse> =
        ApiResponse(success = true, data = profileService.saveFinancialProfile(customerId, request))

    @PutMapping("/{customerId}/communication-preferences")
    fun saveCommunicationPreference(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: CommunicationPreferenceRequest
    ): ApiResponse<CommunicationPreferenceResponse> =
        ApiResponse(success = true, data = profileService.saveCommunicationPreference(customerId, request))
}
