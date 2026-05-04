package com.bank.rm.notification.dto

import com.bank.rm.common.dto.*
import java.util.UUID

data class SendNotificationRequest(
    val customerId: UUID,
    val type: NotificationType,
    val channel: CommunicationChannel,
    val subject: String,
    val body: String,
    val recipientAddress: String,
    val fallbackChannel: CommunicationChannel? = null
)

data class NotificationResponse(
    val id: UUID,
    val customerId: UUID,
    val type: NotificationType,
    val channel: CommunicationChannel,
    val subject: String,
    val status: NotificationStatus,
    val sentAt: String?,
    val externalId: String?
)
