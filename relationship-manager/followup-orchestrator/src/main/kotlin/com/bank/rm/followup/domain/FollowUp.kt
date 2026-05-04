package com.bank.rm.followup.domain

import com.bank.rm.common.dto.*
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "followup_schedules")
data class FollowUpSchedule(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val frequency: FollowUpFrequency,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: CommunicationChannel,

    var nextFollowUpAt: LocalDateTime,
    var isActive: Boolean = true,
    var totalCompleted: Int = 0,
    var totalMissed: Int = 0,

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "followup_instances")
data class FollowUpInstance(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val scheduleId: UUID,

    @Column(nullable = false)
    val customerId: UUID,

    val scheduledAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: FollowUpStatus = FollowUpStatus.SCHEDULED,

    @Column(columnDefinition = "jsonb")
    var agenda: String = "{}",

    @Column(columnDefinition = "jsonb")
    var previousSummary: String? = null,

    var conversationId: UUID? = null,
    var completedAt: Instant? = null,
    var reminderSentAt: Instant? = null,

    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "action_items")
data class ActionItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    val followUpInstanceId: UUID? = null,
    val title: String,
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val assignedTo: SenderType, // CUSTOMER or AI (RM)

    @Enumerated(EnumType.STRING)
    var status: ActionItemStatus = ActionItemStatus.OPEN,

    val dueDate: LocalDateTime? = null,
    var completedAt: Instant? = null,

    val createdAt: Instant = Instant.now()
)
