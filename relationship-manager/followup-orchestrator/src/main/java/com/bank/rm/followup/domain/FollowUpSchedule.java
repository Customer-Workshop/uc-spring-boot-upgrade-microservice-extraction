package com.bank.rm.followup.domain;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.FollowUpFrequency;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "followup_schedules")
public class FollowUpSchedule {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationChannel channel;

    @Column(name = "next_follow_up_at", nullable = false)
    private LocalDateTime nextFollowUpAt;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "total_completed")
    private int totalCompleted;

    @Column(name = "total_missed")
    private int totalMissed;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected FollowUpSchedule() {}

    public FollowUpSchedule(UUID id, UUID customerId, FollowUpFrequency frequency,
                             CommunicationChannel channel, LocalDateTime nextFollowUpAt) {
        this.id = id;
        this.customerId = customerId;
        this.frequency = frequency;
        this.channel = channel;
        this.nextFollowUpAt = nextFollowUpAt;
        this.active = true;
        this.totalCompleted = 0;
        this.totalMissed = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public FollowUpFrequency getFrequency() { return frequency; }
    public CommunicationChannel getChannel() { return channel; }
    public LocalDateTime getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(int totalCompleted) { this.totalCompleted = totalCompleted; }
    public int getTotalMissed() { return totalMissed; }
    public void setTotalMissed(int totalMissed) { this.totalMissed = totalMissed; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
