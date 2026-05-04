package com.bank.rm.conversation.domain;

import com.bank.rm.common.dto.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "anonymous_session_id")
    private UUID anonymousSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase")
    private ConversationPhase currentPhase;

    @Enumerated(EnumType.STRING)
    private ConversationStatus status;

    @Enumerated(EnumType.STRING)
    private CommunicationChannel channel;

    @Column(name = "extracted_data", columnDefinition = "jsonb")
    private String extractedData;

    @Column(name = "llm_context", columnDefinition = "jsonb")
    private String llmContext;

    @Column(name = "assigned_rm_id")
    private UUID assignedRmId;

    private String summary;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Conversation() {}

    public Conversation(UUID id, UUID customerId, UUID anonymousSessionId, CommunicationChannel channel) {
        this.id = id;
        this.customerId = customerId;
        this.anonymousSessionId = anonymousSessionId;
        this.currentPhase = ConversationPhase.GREETING;
        this.status = ConversationStatus.ACTIVE;
        this.channel = channel;
        this.extractedData = "{}";
        this.llmContext = "{}";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAnonymousSessionId() { return anonymousSessionId; }
    public ConversationPhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(ConversationPhase currentPhase) { this.currentPhase = currentPhase; }
    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }
    public CommunicationChannel getChannel() { return channel; }
    public String getExtractedData() { return extractedData; }
    public void setExtractedData(String extractedData) { this.extractedData = extractedData; }
    public String getLlmContext() { return llmContext; }
    public void setLlmContext(String llmContext) { this.llmContext = llmContext; }
    public UUID getAssignedRmId() { return assignedRmId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
