package com.bank.rm.common.event;

import com.bank.rm.common.dto.CommunicationChannel;
import java.util.UUID;

public class ConversationStartedEvent extends DomainEvent {
    private final UUID conversationId;
    private final UUID customerId;
    private final UUID anonymousSessionId;
    private final CommunicationChannel channel;

    public ConversationStartedEvent(UUID conversationId, UUID customerId,
                                     UUID anonymousSessionId, CommunicationChannel channel) {
        super("conversation-service");
        this.conversationId = conversationId;
        this.customerId = customerId;
        this.anonymousSessionId = anonymousSessionId;
        this.channel = channel;
    }

    public UUID getConversationId() { return conversationId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAnonymousSessionId() { return anonymousSessionId; }
    public CommunicationChannel getChannel() { return channel; }
}
