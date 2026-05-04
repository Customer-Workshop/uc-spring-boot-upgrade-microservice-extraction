package com.bank.rm.admin.controller;

import com.bank.rm.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "service", "admin-service",
            "status", "UP",
            "version", "1.0.0"
        )));
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStatus() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "auth-service", Map.of("port", 8081, "status", "configured"),
            "customer-profile-service", Map.of("port", 8082, "status", "configured"),
            "conversation-service", Map.of("port", 8083, "status", "configured"),
            "risk-profiling-service", Map.of("port", 8084, "status", "configured"),
            "product-catalog-service", Map.of("port", 8085, "status", "configured"),
            "recommendation-service", Map.of("port", 8086, "status", "configured"),
            "wealth-projection-service", Map.of("port", 8087, "status", "configured"),
            "followup-orchestrator", Map.of("port", 8088, "status", "configured"),
            "notification-service", Map.of("port", 8089, "status", "configured"),
            "analytics-service", Map.of("port", 8090, "status", "configured"),
            "admin-service", Map.of("port", 8091, "status", "configured"),
            "reminder-service", Map.of("port", 8092, "status", "configured"),
            "api-gateway", Map.of("port", 8080, "status", "configured")
        )));
    }
}
