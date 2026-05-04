package com.bank.rm.followup.domain;

import com.bank.rm.common.dto.FollowUpStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "followup_instances")
public class FollowUpInstance {
    @Id
    private UUID id;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpStatus status;

    @Column(columnDefinition = "jsonb")
    private String agenda;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected FollowUpInstance() {}

    public FollowUpInstance(UUID id, UUID scheduleId, UUID customerId,
                             LocalDateTime scheduledAt, String agenda) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.customerId = customerId;
        this.scheduledAt = scheduledAt;
        this.status = FollowUpStatus.SCHEDULED;
        this.agenda = agenda;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getScheduleId() { return scheduleId; }
    public UUID getCustomerId() { return customerId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public FollowUpStatus getStatus() { return status; }
    public void setStatus(FollowUpStatus status) { this.status = status; }
    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
