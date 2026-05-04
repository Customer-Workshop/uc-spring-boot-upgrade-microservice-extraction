package com.bank.rm.notification.domain

import com.bank.rm.common.dto.*
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: CommunicationChannel,

    @Column(nullable = false)
    val subject: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val body: String,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus = NotificationStatus.PENDING,

    val recipientAddress: String,
    var externalId: String? = null,
    var sentAt: Instant? = null,
    var deliveredAt: Instant? = null,
    var failureReason: String? = null,
    var retryCount: Int = 0,

    val createdAt: Instant = Instant.now()
)
