package com.bank.rm.reminder.service

import com.bank.rm.common.dto.CommunicationChannel
import com.bank.rm.common.event.*
import com.bank.rm.reminder.domain.Reminder
import com.bank.rm.reminder.repository.ReminderRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.FOLLOWUP_EVENTS], groupId = "reminder-service")
    fun handleFollowUpScheduled(event: FollowUpScheduledEvent) {
        log.info("Scheduling reminder for customer {} via {}", event.customerId, event.channel)

        // Create reminders: 24h before and 1h before
        val followUpTime = LocalDateTime.now().plusDays(7) // placeholder; real impl reads from schedule

        createReminder(event.customerId, null, event.channel, followUpTime.minusHours(24),
            "Reminder: Your portfolio review is tomorrow. We'll discuss your progress and next steps.")

        createReminder(event.customerId, null, event.channel, followUpTime.minusHours(1),
            "Your portfolio review is in 1 hour. Ready to catch up on your financial goals?")
    }

    @Transactional
    fun createReminder(
        customerId: UUID,
        followUpInstanceId: UUID?,
        channel: CommunicationChannel,
        scheduledAt: LocalDateTime,
        message: String
    ): Reminder {
        val reminder = Reminder(
            customerId = customerId,
            followUpInstanceId = followUpInstanceId,
            channel = channel,
            scheduledAt = scheduledAt,
            message = message
        )
        return reminderRepository.save(reminder)
    }

    // Run every 5 minutes to check for due reminders
    @Scheduled(fixedRate = 300000)
    @Transactional
    fun processReminders() {
        val now = LocalDateTime.now()
        val dueReminders = reminderRepository.findByStatusAndScheduledAtBefore("PENDING", now)

        dueReminders.forEach { reminder ->
            try {
                kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION_EVENTS,
                    reminder.customerId.toString(),
                    FollowUpDueEvent(
                        customerId = reminder.customerId,
                        instanceId = reminder.followUpInstanceId ?: UUID.randomUUID(),
                        channel = reminder.channel
                    )
                )
                reminder.status = "SENT"
                reminder.sentAt = Instant.now()
                reminderRepository.save(reminder)
                log.info("Reminder sent for customer {} via {}", reminder.customerId, reminder.channel)
            } catch (e: Exception) {
                log.error("Failed to send reminder {}: {}", reminder.id, e.message)
                reminder.retryCount++
                if (reminder.retryCount >= 3) {
                    reminder.status = "FAILED"
                }
                reminderRepository.save(reminder)
            }
        }
    }

    fun getRemindersForCustomer(customerId: UUID): List<Reminder> =
        reminderRepository.findByCustomerIdOrderByScheduledAtDesc(customerId)
}
