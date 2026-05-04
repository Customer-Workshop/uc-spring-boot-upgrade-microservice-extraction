package com.bank.rm.analytics.service;

import com.bank.rm.analytics.dto.AnalyticsDtos.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsAggregator {
    private final AtomicLong totalConversations = new AtomicLong(0);
    private final AtomicLong completedConversations = new AtomicLong(0);
    private final AtomicLong totalNotifications = new AtomicLong(0);
    private final Map<String, AtomicLong> channelCounts = new ConcurrentHashMap<>();

    @KafkaListener(topics = "conversation.events", groupId = "analytics-consumer", autoStartup = "false")
    public void handleConversationEvent(String event) {
        totalConversations.incrementAndGet();
        if (event.contains("COMPLETED")) {
            completedConversations.incrementAndGet();
        }
    }

    @KafkaListener(topics = "notification.events", groupId = "analytics-consumer", autoStartup = "false")
    public void handleNotificationEvent(String event) {
        totalNotifications.incrementAndGet();
    }

    public DashboardMetrics getDashboardMetrics() {
        return new DashboardMetrics(
            totalConversations.get(),
            completedConversations.get(),
            totalConversations.get() > 0 ?
                (completedConversations.get() * 100.0 / totalConversations.get()) : 0,
            totalNotifications.get()
        );
    }
}
