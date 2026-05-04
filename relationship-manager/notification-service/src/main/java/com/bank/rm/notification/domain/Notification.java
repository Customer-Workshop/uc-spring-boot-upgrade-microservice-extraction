package com.bank.rm.notification.domain;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.NotificationStatus;
import com.bank.rm.common.dto.NotificationType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationChannel channel;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "recipient_address", nullable = false)
    private String recipientAddress;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Notification() {}

    public Notification(UUID id, UUID customerId, NotificationType type, CommunicationChannel channel,
                         String subject, String body, String recipientAddress) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
        this.recipientAddress = recipientAddress;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public NotificationType getType() { return type; }
    public CommunicationChannel getChannel() { return channel; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getRecipientAddress() { return recipientAddress; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
