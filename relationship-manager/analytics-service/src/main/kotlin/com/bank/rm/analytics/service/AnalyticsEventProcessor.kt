package com.bank.rm.analytics.service

import com.bank.rm.common.event.*
import com.bank.rm.analytics.dto.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class AnalyticsEventProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    // In-memory metrics (production would use Elasticsearch/TimescaleDB)
    private val conversationCount = AtomicLong(0)
    private val riskAssessmentCount = AtomicLong(0)
    private val recommendationCount = AtomicLong(0)
    private val notificationCount = AtomicLong(0)
    private val followUpCount = AtomicLong(0)

    private val riskDistribution = ConcurrentHashMap<String, AtomicLong>()
    private val channelDistribution = ConcurrentHashMap<String, AtomicLong>()
    private val phaseCompletionRates = ConcurrentHashMap<String, AtomicLong>()

    @KafkaListener(topics = [KafkaTopics.CONVERSATION_EVENTS], groupId = "analytics-service")
    fun processConversationEvent(event: ConversationStartedEvent) {
        conversationCount.incrementAndGet()
        channelDistribution.computeIfAbsent(event.channel.name) { AtomicLong() }.incrementAndGet()
        log.debug("Analytics: conversation started, total: {}", conversationCount.get())
    }

    @KafkaListener(topics = [KafkaTopics.RISK_EVENTS], groupId = "analytics-service")
    fun processRiskEvent(event: RiskAssessedEvent) {
        riskAssessmentCount.incrementAndGet()
        riskDistribution.computeIfAbsent(event.riskCategory.name) { AtomicLong() }.incrementAndGet()
        log.debug("Analytics: risk assessed, category: {}", event.riskCategory)
    }

    @KafkaListener(topics = [KafkaTopics.RECOMMENDATION_EVENTS], groupId = "analytics-service")
    fun processRecommendationEvent(event: RecommendationGeneratedEvent) {
        recommendationCount.incrementAndGet()
    }

    @KafkaListener(topics = [KafkaTopics.NOTIFICATION_EVENTS], groupId = "analytics-service")
    fun processNotificationEvent(event: NotificationSentEvent) {
        notificationCount.incrementAndGet()
        channelDistribution.computeIfAbsent("notif_${event.channel.name}") { AtomicLong() }.incrementAndGet()
    }

    @KafkaListener(topics = [KafkaTopics.FOLLOWUP_EVENTS], groupId = "analytics-service")
    fun processFollowUpEvent(event: FollowUpScheduledEvent) {
        followUpCount.incrementAndGet()
    }

    fun getDashboardMetrics(): DashboardMetrics = DashboardMetrics(
        totalConversations = conversationCount.get(),
        totalRiskAssessments = riskAssessmentCount.get(),
        totalRecommendations = recommendationCount.get(),
        totalNotifications = notificationCount.get(),
        totalFollowUps = followUpCount.get(),
        riskDistribution = riskDistribution.mapValues { it.value.get() },
        channelDistribution = channelDistribution.mapValues { it.value.get() },
        timestamp = Instant.now().toString()
    )

    fun getConversionFunnel(): ConversionFunnel = ConversionFunnel(
        started = conversationCount.get(),
        personalCompleted = phaseCompletionRates.getOrDefault("PERSONAL", AtomicLong()).get(),
        financialCompleted = phaseCompletionRates.getOrDefault("FINANCIAL", AtomicLong()).get(),
        riskAssessed = riskAssessmentCount.get(),
        recommendationViewed = recommendationCount.get(),
        followUpScheduled = followUpCount.get()
    )
}
