package com.bank.rm.reminder.service;

import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.reminder.domain.Reminder;
import com.bank.rm.reminder.dto.ReminderDtos.*;
import com.bank.rm.reminder.repository.ReminderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ReminderService {
    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private final ReminderRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ReminderService(ReminderRepository repository, KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReminderResponse createReminder(CreateReminderRequest request) {
        Reminder reminder = new Reminder(UUID.randomUUID(), request.customerId(),
                request.followUpInstanceId(), request.channel(), request.scheduledAt(), request.message());
        repository.save(reminder);
        return new ReminderResponse(reminder.getId(), request.customerId(),
                request.channel(), request.scheduledAt(), "PENDING");
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processReminders() {
        List<Reminder> due = repository.findPendingDue(LocalDateTime.now());
        for (Reminder reminder : due) {
            try {
                // Publish to notification service
                kafkaTemplate.send(KafkaTopics.REMINDER_EVENTS,
                        objectMapper.writeValueAsString(new ReminderDueEvent(
                                reminder.getCustomerId(), reminder.getId(), reminder.getChannel().name(),
                                reminder.getMessage())));
                reminder.setStatus("SENT");
                reminder.setSentAt(Instant.now());
                repository.save(reminder);
            } catch (Exception e) {
                log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }
    }

    private record ReminderDueEvent(UUID customerId, UUID reminderId, String channel, String message) {}
}
