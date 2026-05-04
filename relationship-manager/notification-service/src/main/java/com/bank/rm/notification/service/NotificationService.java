package com.bank.rm.notification.service;

import com.bank.rm.common.dto.NotificationStatus;
import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.common.event.NotificationSentEvent;
import com.bank.rm.notification.channel.NotificationRouter;
import com.bank.rm.notification.channel.NotificationRouter.DeliveryResult;
import com.bank.rm.notification.domain.Notification;
import com.bank.rm.notification.dto.NotificationDtos.*;
import com.bank.rm.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository repository;
    private final NotificationRouter router;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository repository, NotificationRouter router,
                                KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.router = router;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        Notification notification = new Notification(UUID.randomUUID(), request.customerId(),
                request.type(), request.channel(), request.subject(), request.body(), request.recipientAddress());
        notification.setStatus(NotificationStatus.QUEUED);
        repository.save(notification);

        DeliveryResult result = router.route(notification);
        if (result.success()) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setExternalId(result.externalId());
            notification.setSentAt(Instant.now());
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(result.failureReason());
        }
        repository.save(notification);

        publishEvent(new NotificationSentEvent(request.customerId(), notification.getId(),
                request.channel(), notification.getStatus()));

        return new NotificationResponse(notification.getId(), request.customerId(),
                request.type(), request.channel(), notification.getStatus(), request.subject(), Instant.now());
    }

    private void publishEvent(NotificationSentEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.NOTIFICATION_EVENTS, objectMapper.writeValueAsString(event));
        } catch (Exception e) { log.error("Failed to publish notification event: {}", e.getMessage()); }
    }
}
