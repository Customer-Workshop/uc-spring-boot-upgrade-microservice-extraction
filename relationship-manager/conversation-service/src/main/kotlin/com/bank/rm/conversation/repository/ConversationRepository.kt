package com.bank.rm.conversation.repository

import com.bank.rm.common.dto.ConversationStatus
import com.bank.rm.conversation.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ConversationRepository : JpaRepository<Conversation, UUID> {
    fun findByCustomerIdAndStatus(customerId: UUID, status: ConversationStatus): List<Conversation>
    fun findByAnonymousSessionId(sessionId: UUID): List<Conversation>

    @Query("SELECT c FROM Conversation c WHERE c.customerId = :customerId ORDER BY c.lastMessageAt DESC")
    fun findByCustomerIdOrderByLastMessageAtDesc(customerId: UUID): List<Conversation>
}

interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByConversationIdOrderByCreatedAtAsc(conversationId: UUID): List<Message>

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC LIMIT :limit")
    fun findRecentMessages(conversationId: UUID, limit: Int): List<Message>
}

interface ConversationTemplateRepository : JpaRepository<ConversationTemplate, UUID> {
    fun findByPhaseAndIsActiveTrue(phase: com.bank.rm.common.dto.ConversationPhase): ConversationTemplate?
}
