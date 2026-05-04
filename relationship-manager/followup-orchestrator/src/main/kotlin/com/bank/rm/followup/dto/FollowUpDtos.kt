package com.bank.rm.followup.dto

import com.bank.rm.common.dto.*
import java.time.LocalDateTime
import java.util.UUID

data class CreateScheduleRequest(
    val frequency: FollowUpFrequency,
    val channel: CommunicationChannel,
    val startDate: LocalDateTime? = null
)

data class ScheduleResponse(
    val id: UUID,
    val customerId: UUID,
    val frequency: FollowUpFrequency,
    val channel: CommunicationChannel,
    val nextFollowUpAt: String,
    val isActive: Boolean,
    val totalCompleted: Int,
    val totalMissed: Int
)

data class FollowUpInstanceResponse(
    val id: UUID,
    val scheduleId: UUID,
    val customerId: UUID,
    val scheduledAt: String,
    val status: FollowUpStatus,
    val agenda: String,
    val conversationId: UUID?
)
