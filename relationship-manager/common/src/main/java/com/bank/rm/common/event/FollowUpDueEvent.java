package com.bank.rm.common.event;

import com.bank.rm.common.dto.CommunicationChannel;
import java.util.UUID;

public class FollowUpDueEvent extends DomainEvent {
    private final UUID customerId;
    private final UUID instanceId;
    private final CommunicationChannel channel;

    public FollowUpDueEvent(UUID customerId, UUID instanceId, CommunicationChannel channel) {
        super("reminder-service");
        this.customerId = customerId;
        this.instanceId = instanceId;
        this.channel = channel;
    }

    public UUID getCustomerId() { return customerId; }
    public UUID getInstanceId() { return instanceId; }
    public CommunicationChannel getChannel() { return channel; }
}
