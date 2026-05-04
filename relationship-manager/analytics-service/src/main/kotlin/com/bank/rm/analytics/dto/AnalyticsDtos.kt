package com.bank.rm.analytics.dto

data class DashboardMetrics(
    val totalConversations: Long,
    val totalRiskAssessments: Long,
    val totalRecommendations: Long,
    val totalNotifications: Long,
    val totalFollowUps: Long,
    val riskDistribution: Map<String, Long>,
    val channelDistribution: Map<String, Long>,
    val timestamp: String
)

data class ConversionFunnel(
    val started: Long,
    val personalCompleted: Long,
    val financialCompleted: Long,
    val riskAssessed: Long,
    val recommendationViewed: Long,
    val followUpScheduled: Long
)
