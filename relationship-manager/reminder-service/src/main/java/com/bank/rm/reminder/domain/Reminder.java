package com.bank.rm.reminder.domain;

import com.bank.rm.common.dto.CommunicationChannel;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class Reminder {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "follow_up_instance_id")
    private UUID followUpInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationChannel channel;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private String status;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Reminder() {}

    public Reminder(UUID id, UUID customerId, UUID followUpInstanceId,
                     CommunicationChannel channel, LocalDateTime scheduledAt, String message) {
        this.id = id;
        this.customerId = customerId;
        this.followUpInstanceId = followUpInstanceId;
        this.channel = channel;
        this.scheduledAt = scheduledAt;
        this.message = message;
        this.status = "PENDING";
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public CommunicationChannel getChannel() { return channel; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
