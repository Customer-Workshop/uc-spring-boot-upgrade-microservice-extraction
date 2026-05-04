package com.bank.rm.notification.service

import com.bank.rm.common.dto.CommunicationChannel
import com.bank.rm.common.dto.NotificationStatus
import com.bank.rm.common.dto.NotificationType
import com.bank.rm.common.event.*
import com.bank.rm.notification.domain.Notification
import com.bank.rm.notification.dto.*
import com.bank.rm.notification.repository.NotificationRepository
import com.bank.rm.notification.service.channel.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class NotificationRouter(
    private val notificationRepository: NotificationRepository,
    private val smsChannel: SmsChannelAdapter,
    private val emailChannel: EmailChannelAdapter,
    private val whatsappChannel: WhatsAppChannelAdapter,
    private val pushChannel: PushNotificationAdapter,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun send(request: SendNotificationRequest): NotificationResponse {
        val notification = Notification(
            customerId = request.customerId,
            type = request.type,
            channel = request.channel,
            subject = request.subject,
            body = request.body,
            recipientAddress = request.recipientAddress,
            status = NotificationStatus.QUEUED
        )
        notificationRepository.save(notification)

        return try {
            val result = routeToChannel(notification)
            notification.status = NotificationStatus.SENT
            notification.sentAt = Instant.now()
            notification.externalId = result.externalId
            notificationRepository.save(notification)

            kafkaTemplate.send(
                KafkaTopics.NOTIFICATION_EVENTS,
                notification.customerId.toString(),
                NotificationSentEvent(
                    customerId = notification.customerId,
                    notificationId = notification.id,
                    channel = notification.channel,
                    status = NotificationStatus.SENT
                )
            )

            notification.toResponse()
        } catch (e: Exception) {
            log.error("Failed to send notification via ${request.channel}: ${e.message}", e)
            notification.status = NotificationStatus.FAILED
            notification.failureReason = e.message
            notification.retryCount++
            notificationRepository.save(notification)

            // Try fallback channel
            if (request.fallbackChannel != null) {
                log.info("Attempting fallback channel: ${request.fallbackChannel}")
                return send(request.copy(channel = request.fallbackChannel, fallbackChannel = null))
            }

            notification.toResponse()
        }
    }

    @KafkaListener(topics = [KafkaTopics.REMINDER_EVENTS], groupId = "notification-service")
    fun handleReminderEvent(event: FollowUpDueEvent) {
        log.info("Processing follow-up reminder for customer {}", event.customerId)
        send(SendNotificationRequest(
            customerId = event.customerId,
            type = NotificationType.FOLLOWUP_REMINDER,
            channel = event.channel,
            subject = "Your follow-up review is due",
            body = "Hi! Your scheduled portfolio review is ready. Let's catch up on your financial progress.",
            recipientAddress = "placeholder",
            fallbackChannel = CommunicationChannel.EMAIL
        ))
    }

    fun getNotificationHistory(customerId: UUID): List<NotificationResponse> =
        notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).map { it.toResponse() }

    private fun routeToChannel(notification: Notification): ChannelDeliveryResult = when (notification.channel) {
        CommunicationChannel.SMS -> smsChannel.send(notification)
        CommunicationChannel.EMAIL -> emailChannel.send(notification)
        CommunicationChannel.WHATSAPP -> whatsappChannel.send(notification)
        CommunicationChannel.PUSH -> pushChannel.send(notification)
        CommunicationChannel.PHONE -> {
            log.info("Phone channel: queuing for human RM callback")
            ChannelDeliveryResult(success = true, externalId = "phone-queue-${UUID.randomUUID()}")
        }
        else -> throw IllegalArgumentException("Unsupported notification channel: ${notification.channel}")
    }

    private fun Notification.toResponse() = NotificationResponse(
        id, customerId, type, channel, subject, status,
        sentAt?.toString(), externalId
    )
}

data class ChannelDeliveryResult(
    val success: Boolean,
    val externalId: String? = null,
    val error: String? = null
)
