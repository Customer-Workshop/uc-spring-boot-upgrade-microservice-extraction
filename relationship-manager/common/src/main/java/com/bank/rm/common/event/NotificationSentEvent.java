package com.bank.rm.common.event;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.NotificationStatus;
import java.util.UUID;

public class NotificationSentEvent extends DomainEvent {
    private final UUID customerId;
    private final UUID notificationId;
    private final CommunicationChannel channel;
    private final NotificationStatus status;

    public NotificationSentEvent(UUID customerId, UUID notificationId,
                                  CommunicationChannel channel, NotificationStatus status) {
        super("notification-service");
        this.customerId = customerId;
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
    }

    public UUID getCustomerId() { return customerId; }
    public UUID getNotificationId() { return notificationId; }
    public CommunicationChannel getChannel() { return channel; }
    public NotificationStatus getStatus() { return status; }
}
