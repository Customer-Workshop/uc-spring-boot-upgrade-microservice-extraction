package com.bank.rm.conversation.dto

import com.bank.rm.common.dto.*
import java.util.UUID

data class StartConversationRequest(
    val customerId: UUID? = null,
    val anonymousSessionId: UUID? = null,
    val channel: CommunicationChannel = CommunicationChannel.WEB
)

data class ConversationResponse(
    val conversationId: UUID,
    val currentPhase: ConversationPhase,
    val status: ConversationStatus,
    val channel: CommunicationChannel,
    val createdAt: String,
    val lastMessageAt: String
)

data class SendMessageRequest(
    val content: String,
    val contentType: ContentType = ContentType.TEXT
)

data class MessageResponse(
    val messageId: UUID,
    val conversationId: UUID,
    val senderType: SenderType,
    val content: String,
    val contentType: ContentType,
    val phase: ConversationPhase?,
    val entitiesExtracted: Map<String, Any>?,
    val createdAt: String
)

data class ConversationHistoryResponse(
    val conversationId: UUID,
    val currentPhase: ConversationPhase,
    val status: ConversationStatus,
    val messages: List<MessageResponse>,
    val extractedData: Map<String, Any>
)

data class HandoffRequest(
    val conversationId: UUID,
    val reason: String? = null
)

data class ConversationSummaryResponse(
    val conversationId: UUID,
    val summary: String,
    val phasesCompleted: List<ConversationPhase>,
    val extractedData: Map<String, Any>
)
