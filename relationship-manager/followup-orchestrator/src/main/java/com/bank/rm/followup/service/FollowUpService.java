package com.bank.rm.followup.service;

import com.bank.rm.common.dto.FollowUpFrequency;
import com.bank.rm.common.dto.FollowUpStatus;
import com.bank.rm.common.event.FollowUpDueEvent;
import com.bank.rm.common.event.FollowUpScheduledEvent;
import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.followup.domain.FollowUpInstance;
import com.bank.rm.followup.domain.FollowUpSchedule;
import com.bank.rm.followup.dto.FollowUpDtos.*;
import com.bank.rm.followup.repository.FollowUpInstanceRepository;
import com.bank.rm.followup.repository.FollowUpScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FollowUpService {
    private static final Logger log = LoggerFactory.getLogger(FollowUpService.class);
    private final FollowUpScheduleRepository scheduleRepository;
    private final FollowUpInstanceRepository instanceRepository;
    private final ChatLanguageModel chatModel;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FollowUpService(FollowUpScheduleRepository scheduleRepository,
                            FollowUpInstanceRepository instanceRepository,
                            ChatLanguageModel chatModel,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.scheduleRepository = scheduleRepository;
        this.instanceRepository = instanceRepository;
        this.chatModel = chatModel;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        UUID scheduleId = UUID.randomUUID();
        LocalDateTime nextFollowUp = computeNextFollowUp(request.frequency(), LocalDateTime.now());
        FollowUpSchedule schedule = new FollowUpSchedule(
                scheduleId, request.customerId(), request.frequency(), request.channel(), nextFollowUp);
        scheduleRepository.save(schedule);

        publishEvent(KafkaTopics.FOLLOWUP_EVENTS,
                new FollowUpScheduledEvent(request.customerId(), scheduleId, request.frequency(), request.channel()));

        return new ScheduleResponse(scheduleId, request.customerId(), request.frequency(),
                request.channel(), nextFollowUp, true, 0);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledFollowUps() {
        List<FollowUpSchedule> due = scheduleRepository.findDueSchedules(LocalDateTime.now());
        for (FollowUpSchedule schedule : due) {
            try {
                String agenda = generateAgenda(schedule.getCustomerId());
                FollowUpInstance instance = new FollowUpInstance(
                        UUID.randomUUID(), schedule.getId(), schedule.getCustomerId(),
                        schedule.getNextFollowUpAt(), agenda);
                instanceRepository.save(instance);

                publishEvent(KafkaTopics.FOLLOWUP_EVENTS,
                        new FollowUpDueEvent(schedule.getCustomerId(), instance.getId(), schedule.getChannel()));

                schedule.setNextFollowUpAt(computeNextFollowUp(schedule.getFrequency(), schedule.getNextFollowUpAt()));
                schedule.setUpdatedAt(Instant.now());
                scheduleRepository.save(schedule);
            } catch (Exception e) {
                log.error("Failed to process follow-up for customer {}: {}", schedule.getCustomerId(), e.getMessage());
            }
        }
    }

    private String generateAgenda(UUID customerId) {
        try {
            String prompt = "Generate a brief follow-up meeting agenda for a banking customer review. " +
                    "Include: portfolio performance review, risk assessment update, new product recommendations, " +
                    "and action items from previous meeting. Return as JSON array of agenda items.";
            return chatModel.generate(prompt);
        } catch (Exception e) {
            return "[\"Review portfolio performance\",\"Update risk profile\",\"Discuss new recommendations\",\"Review action items\"]";
        }
    }

    private LocalDateTime computeNextFollowUp(FollowUpFrequency frequency, LocalDateTime from) {
        return switch (frequency) {
            case WEEKLY -> from.plusWeeks(1);
            case BIWEEKLY -> from.plusWeeks(2);
            case MONTHLY -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
        };
    }

    private void publishEvent(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
        } catch (Exception e) { log.error("Failed to publish event: {}", e.getMessage()); }
    }
}
