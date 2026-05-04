package com.bank.rm.reminder.domain

import com.bank.rm.common.dto.CommunicationChannel
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reminders")
data class Reminder(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    val followUpInstanceId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: CommunicationChannel,

    val scheduledAt: LocalDateTime,
    val message: String,

    @Column(nullable = false)
    var status: String = "PENDING", // PENDING, SENT, FAILED, CANCELLED

    var sentAt: Instant? = null,
    var retryCount: Int = 0,

    val createdAt: Instant = Instant.now()
)
