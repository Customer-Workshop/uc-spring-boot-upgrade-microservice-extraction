package com.bank.rm.admin.controller

import com.bank.rm.common.dto.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminController {

    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, String>> =
        ApiResponse(success = true, data = mapOf(
            "status" to "UP",
            "service" to "admin-service",
            "version" to "1.0.0"
        ))

    @GetMapping("/config/conversation-templates")
    fun getConversationTemplates(): ApiResponse<Map<String, Any>> =
        ApiResponse(success = true, data = mapOf(
            "phases" to listOf("GREETING", "PERSONAL", "FINANCIAL", "GOALS", "RISK_ASSESSMENT",
                "RECOMMENDATION", "CHANNEL_PREF", "FOLLOWUP_SCHEDULE", "COMPLETED"),
            "aiModel" to "gpt-4o",
            "maxConversationLength" to 100,
            "sessionTimeoutMinutes" to 30
        ))

    @GetMapping("/config/risk-model")
    fun getRiskModelConfig(): ApiResponse<Map<String, Any>> =
        ApiResponse(success = true, data = mapOf(
            "model" to "xgboost-v1.0",
            "features" to 15,
            "riskCategories" to listOf("CONSERVATIVE", "MODERATE", "AGGRESSIVE", "VERY_AGGRESSIVE"),
            "refreshIntervalDays" to 90,
            "shapEnabled" to true
        ))

    @GetMapping("/config/notification-channels")
    fun getNotificationConfig(): ApiResponse<Map<String, Any>> =
        ApiResponse(success = true, data = mapOf(
            "channels" to mapOf(
                "SMS" to mapOf("provider" to "Twilio", "enabled" to true),
                "EMAIL" to mapOf("provider" to "SendGrid", "enabled" to true),
                "WHATSAPP" to mapOf("provider" to "Twilio", "enabled" to true),
                "PHONE" to mapOf("provider" to "Manual Queue", "enabled" to true),
                "PUSH" to mapOf("provider" to "FCM", "enabled" to false)
            )
        ))
}
