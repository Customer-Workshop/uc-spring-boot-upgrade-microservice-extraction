package com.bank.rm.analytics.controller

import com.bank.rm.analytics.dto.*
import com.bank.rm.analytics.service.AnalyticsEventProcessor
import com.bank.rm.common.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(private val analyticsService: AnalyticsEventProcessor) {

    @GetMapping("/dashboard")
    fun getDashboard(): ApiResponse<DashboardMetrics> =
        ApiResponse(success = true, data = analyticsService.getDashboardMetrics())

    @GetMapping("/funnel")
    fun getConversionFunnel(): ApiResponse<ConversionFunnel> =
        ApiResponse(success = true, data = analyticsService.getConversionFunnel())
}
