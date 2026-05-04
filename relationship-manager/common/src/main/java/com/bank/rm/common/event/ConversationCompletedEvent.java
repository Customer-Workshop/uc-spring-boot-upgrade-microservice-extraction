package com.bank.rm.common.event;

import com.bank.rm.common.dto.ConversationPhase;
import java.util.List;
import java.util.UUID;

public class ConversationCompletedEvent extends DomainEvent {
    private final UUID conversationId;
    private final UUID customerId;
    private final String summary;
    private final List<ConversationPhase> phasesCompleted;

    public ConversationCompletedEvent(UUID conversationId, UUID customerId,
                                       String summary, List<ConversationPhase> phasesCompleted) {
        super("conversation-service");
        this.conversationId = conversationId;
        this.customerId = customerId;
        this.summary = summary;
        this.phasesCompleted = phasesCompleted;
    }

    public UUID getConversationId() { return conversationId; }
    public UUID getCustomerId() { return customerId; }
    public String getSummary() { return summary; }
    public List<ConversationPhase> getPhasesCompleted() { return phasesCompleted; }
}
