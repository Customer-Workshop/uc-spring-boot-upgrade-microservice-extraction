package com.bank.rm.followup.service

import com.bank.rm.common.dto.*
import com.bank.rm.common.event.*
import com.bank.rm.common.exception.ResourceNotFoundException
import com.bank.rm.followup.domain.*
import com.bank.rm.followup.dto.*
import com.bank.rm.followup.repository.*
import dev.langchain4j.model.chat.ChatLanguageModel
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class FollowUpService(
    private val scheduleRepository: FollowUpScheduleRepository,
    private val instanceRepository: FollowUpInstanceRepository,
    private val actionItemRepository: ActionItemRepository,
    private val chatModel: ChatLanguageModel,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createSchedule(customerId: UUID, request: CreateScheduleRequest): ScheduleResponse {
        val nextFollowUp = calculateNextFollowUp(request.frequency, request.startDate)

        val schedule = FollowUpSchedule(
            customerId = customerId,
            frequency = request.frequency,
            channel = request.channel,
            nextFollowUpAt = nextFollowUp
        )
        scheduleRepository.save(schedule)

        // Create first instance
        val instance = FollowUpInstance(
            scheduleId = schedule.id,
            customerId = customerId,
            scheduledAt = nextFollowUp,
            agenda = generateAgenda(customerId, null)
        )
        instanceRepository.save(instance)

        kafkaTemplate.send(
            KafkaTopics.FOLLOWUP_EVENTS,
            customerId.toString(),
            FollowUpScheduledEvent(
                customerId = customerId,
                scheduleId = schedule.id,
                frequency = request.frequency,
                channel = request.channel
            )
        )

        return schedule.toResponse()
    }

    fun getSchedule(customerId: UUID): ScheduleResponse? {
        val schedule = scheduleRepository.findByCustomerIdAndIsActiveTrue(customerId) ?: return null
        return schedule.toResponse()
    }

    fun getUpcomingInstances(customerId: UUID): List<FollowUpInstanceResponse> =
        instanceRepository.findByCustomerIdAndStatusOrderByScheduledAtAsc(customerId, FollowUpStatus.SCHEDULED)
            .map { it.toResponse() }

    @Transactional
    fun completeFollowUp(instanceId: UUID, conversationId: UUID?): FollowUpInstanceResponse {
        val instance = instanceRepository.findById(instanceId).orElseThrow {
            ResourceNotFoundException("Follow-up instance not found: $instanceId")
        }

        instance.status = FollowUpStatus.COMPLETED
        instance.completedAt = Instant.now()
        instance.conversationId = conversationId
        instanceRepository.save(instance)

        // Update schedule
        val schedule = scheduleRepository.findById(instance.scheduleId).orElse(null)
        if (schedule != null) {
            schedule.totalCompleted++
            val nextFollowUp = calculateNextFollowUp(schedule.frequency, schedule.nextFollowUpAt)
            schedule.nextFollowUpAt = nextFollowUp
            schedule.updatedAt = Instant.now()
            scheduleRepository.save(schedule)

            // Create next instance
            val nextInstance = FollowUpInstance(
                scheduleId = schedule.id,
                customerId = schedule.customerId,
                scheduledAt = nextFollowUp,
                agenda = generateAgenda(schedule.customerId, instance.id)
            )
            instanceRepository.save(nextInstance)
        }

        return instance.toResponse()
    }

    // Processes due follow-ups every hour
    @Scheduled(fixedRate = 3600000)
    fun processDueFollowUps() {
        val now = LocalDateTime.now()
        val dueInstances = instanceRepository.findDueInstances(now)

        dueInstances.forEach { instance ->
            if (instance.reminderSentAt == null) {
                val schedule = scheduleRepository.findById(instance.scheduleId).orElse(null)
                if (schedule != null) {
                    kafkaTemplate.send(
                        KafkaTopics.REMINDER_EVENTS,
                        instance.customerId.toString(),
                        FollowUpDueEvent(
                            customerId = instance.customerId,
                            instanceId = instance.id,
                            channel = schedule.channel
                        )
                    )
                    instance.reminderSentAt = Instant.now()
                    instance.status = FollowUpStatus.REMINDER_SENT
                    instanceRepository.save(instance)
                }
            }
        }
    }

    @KafkaListener(topics = [KafkaTopics.CONVERSATION_EVENTS], groupId = "followup-orchestrator")
    fun handleConversationEvent(event: ConversationCompletedEvent) {
        log.info("Conversation completed for customer {}: scheduling follow-ups", event.customerId)
    }

    private fun generateAgenda(customerId: UUID, previousInstanceId: UUID?): String {
        return try {
            val previousSummary = previousInstanceId?.let {
                instanceRepository.findById(it).orElse(null)?.agenda
            }

            val prompt = """Generate a brief follow-up agenda for a banking relationship review meeting.
${if (previousSummary != null) "Previous meeting notes: $previousSummary" else "This is the first follow-up meeting."}

Include 3-4 bullet points covering:
1. Review of current portfolio performance
2. Any market changes relevant to their risk profile
3. Action items from previous meeting (if any)
4. Topics for discussion

Return as JSON: {"items": ["item1", "item2", ...]}"""

            chatModel.generate(prompt)
        } catch (e: Exception) {
            log.warn("AI agenda generation failed: ${e.message}")
            """{"items": ["Review portfolio performance", "Discuss market updates", "Review action items", "Plan next steps"]}"""
        }
    }

    private fun calculateNextFollowUp(frequency: FollowUpFrequency, from: LocalDateTime?): LocalDateTime {
        val base = from ?: LocalDateTime.now()
        return when (frequency) {
            FollowUpFrequency.WEEKLY -> base.plusWeeks(1)
            FollowUpFrequency.BIWEEKLY -> base.plusWeeks(2)
            FollowUpFrequency.MONTHLY -> base.plusMonths(1)
            FollowUpFrequency.QUARTERLY -> base.plusMonths(3)
        }
    }

    private fun FollowUpSchedule.toResponse() = ScheduleResponse(
        id, customerId, frequency, channel, nextFollowUpAt.toString(), isActive, totalCompleted, totalMissed
    )

    private fun FollowUpInstance.toResponse() = FollowUpInstanceResponse(
        id, scheduleId, customerId, scheduledAt.toString(), status, agenda, conversationId
    )
}
