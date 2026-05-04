package com.bank.rm.conversation.domain;

import com.bank.rm.common.dto.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type")
    private SenderType senderType;

    @Column(name = "sender_id")
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private ContentType contentType;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Enumerated(EnumType.STRING)
    private ConversationPhase phase;

    @Column(name = "entities_extracted", columnDefinition = "jsonb")
    private String entitiesExtracted;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Message() {}

    public Message(UUID id, UUID conversationId, SenderType senderType, UUID senderId,
                   ContentType contentType, String content, ConversationPhase phase) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.contentType = contentType;
        this.content = content;
        this.phase = phase;
        this.metadata = "{}";
        this.entitiesExtracted = "{}";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public SenderType getSenderType() { return senderType; }
    public UUID getSenderId() { return senderId; }
    public ContentType getContentType() { return contentType; }
    public String getContent() { return content; }
    public String getMetadata() { return metadata; }
    public ConversationPhase getPhase() { return phase; }
    public String getEntitiesExtracted() { return entitiesExtracted; }
    public void setEntitiesExtracted(String entitiesExtracted) { this.entitiesExtracted = entitiesExtracted; }
    public Instant getCreatedAt() { return createdAt; }
}
