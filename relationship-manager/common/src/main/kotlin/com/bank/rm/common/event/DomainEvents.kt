package com.bank.rm.common.event

import com.bank.rm.common.dto.*
import java.time.Instant
import java.util.UUID

sealed class DomainEvent {
    abstract val eventId: UUID
    abstract val timestamp: Instant
    abstract val source: String
}

data class ConversationStartedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "conversation-service",
    val conversationId: UUID,
    val customerId: UUID?,
    val anonymousSessionId: UUID?,
    val channel: CommunicationChannel
) : DomainEvent()

data class ConversationCompletedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "conversation-service",
    val conversationId: UUID,
    val customerId: UUID,
    val summary: String?,
    val phasesCompleted: List<ConversationPhase>
) : DomainEvent()

data class ProfileUpdatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "customer-profile-service",
    val customerId: UUID,
    val updatedFields: List<String>
) : DomainEvent()

data class RiskAssessedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "risk-profiling-service",
    val customerId: UUID,
    val riskScore: Double,
    val riskCategory: RiskCategory,
    val previousCategory: RiskCategory?
) : DomainEvent()

data class RecommendationGeneratedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "recommendation-service",
    val customerId: UUID,
    val recommendationId: UUID,
    val riskCategory: RiskCategory
) : DomainEvent()

data class FollowUpScheduledEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "followup-orchestrator",
    val customerId: UUID,
    val scheduleId: UUID,
    val frequency: FollowUpFrequency,
    val channel: CommunicationChannel
) : DomainEvent()

data class FollowUpDueEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "reminder-service",
    val customerId: UUID,
    val instanceId: UUID,
    val channel: CommunicationChannel
) : DomainEvent()

data class NotificationSentEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "notification-service",
    val customerId: UUID,
    val notificationId: UUID,
    val channel: CommunicationChannel,
    val status: NotificationStatus
) : DomainEvent()

data class ProductAddedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val source: String = "product-catalog-service",
    val productId: UUID,
    val name: String,
    val category: ProductCategory
) : DomainEvent()
