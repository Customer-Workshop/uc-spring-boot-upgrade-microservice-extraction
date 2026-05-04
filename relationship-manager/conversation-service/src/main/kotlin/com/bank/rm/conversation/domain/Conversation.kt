package com.bank.rm.conversation.domain

import com.bank.rm.common.dto.*
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "conversations")
data class Conversation(
    @Id
    val id: UUID = UUID.randomUUID(),

    val customerId: UUID? = null,
    val anonymousSessionId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var currentPhase: ConversationPhase = ConversationPhase.GREETING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ConversationStatus = ConversationStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: CommunicationChannel = CommunicationChannel.WEB,

    @Column(columnDefinition = "jsonb")
    var extractedData: String = "{}",

    @Column(columnDefinition = "jsonb")
    var llmContext: String = "{}",

    var assignedRmId: UUID? = null,
    var summary: String? = null,
    var lastMessageAt: Instant = Instant.now(),

    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "messages")
data class Message(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val conversationId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val senderType: SenderType,

    val senderId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val contentType: ContentType = ContentType.TEXT,

    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(columnDefinition = "jsonb")
    val metadata: String = "{}",

    @Enumerated(EnumType.STRING)
    val phase: ConversationPhase? = null,

    @Column(columnDefinition = "jsonb")
    val entitiesExtracted: String? = null,

    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "conversation_templates")
data class ConversationTemplate(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val phase: ConversationPhase,

    val systemPrompt: String,

    @Column(columnDefinition = "jsonb")
    val requiredEntities: String = "[]",

    @Column(columnDefinition = "jsonb")
    val validationRules: String = "{}",

    val nextPhase: String? = null,
    val isActive: Boolean = true,
    val version: Int = 1
)
