package com.bank.rm.notification.dto;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.NotificationStatus;
import com.bank.rm.common.dto.NotificationType;
import java.time.Instant;
import java.util.UUID;

public class NotificationDtos {

    public record SendNotificationRequest(UUID customerId, NotificationType type,
                                           CommunicationChannel channel, String subject,
                                           String body, String recipientAddress) {}

    public record NotificationResponse(UUID notificationId, UUID customerId,
                                        NotificationType type, CommunicationChannel channel,
                                        NotificationStatus status, String subject, Instant createdAt) {}

    private NotificationDtos() {}
}
