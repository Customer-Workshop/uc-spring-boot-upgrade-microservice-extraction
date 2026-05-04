package com.bank.rm.analytics.dto;

public class AnalyticsDtos {
    public record DashboardMetrics(long totalConversations, long completedConversations,
                                    double completionRate, long totalNotifications) {}
    private AnalyticsDtos() {}
}
