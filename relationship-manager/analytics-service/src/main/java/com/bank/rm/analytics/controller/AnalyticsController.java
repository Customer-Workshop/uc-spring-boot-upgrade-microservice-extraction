package com.bank.rm.analytics.controller;

import com.bank.rm.analytics.dto.AnalyticsDtos.DashboardMetrics;
import com.bank.rm.analytics.service.AnalyticsAggregator;
import com.bank.rm.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsAggregator aggregator;

    public AnalyticsController(AnalyticsAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetrics>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(aggregator.getDashboardMetrics()));
    }
}
