package com.bank.rm.common.event;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.FollowUpFrequency;
import java.util.UUID;

public class FollowUpScheduledEvent extends DomainEvent {
    private final UUID customerId;
    private final UUID scheduleId;
    private final FollowUpFrequency frequency;
    private final CommunicationChannel channel;

    public FollowUpScheduledEvent(UUID customerId, UUID scheduleId,
                                   FollowUpFrequency frequency, CommunicationChannel channel) {
        super("followup-orchestrator");
        this.customerId = customerId;
        this.scheduleId = scheduleId;
        this.frequency = frequency;
        this.channel = channel;
    }

    public UUID getCustomerId() { return customerId; }
    public UUID getScheduleId() { return scheduleId; }
    public FollowUpFrequency getFrequency() { return frequency; }
    public CommunicationChannel getChannel() { return channel; }
}
