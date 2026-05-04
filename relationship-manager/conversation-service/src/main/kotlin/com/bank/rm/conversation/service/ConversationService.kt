package com.bank.rm.conversation.service

import com.bank.rm.common.dto.*
import com.bank.rm.common.event.*
import com.bank.rm.common.exception.ResourceNotFoundException
import com.bank.rm.common.exception.ValidationException
import com.bank.rm.conversation.ai.ChatMessage
import com.bank.rm.conversation.ai.ConversationAIAgent
import com.bank.rm.conversation.domain.*
import com.bank.rm.conversation.dto.*
import com.bank.rm.conversation.engine.ConversationStateMachine
import com.bank.rm.conversation.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val aiAgent: ConversationAIAgent,
    private val stateMachine: ConversationStateMachine,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun startConversation(request: StartConversationRequest): ConversationResponse {
        val conversation = Conversation(
            customerId = request.customerId,
            anonymousSessionId = request.anonymousSessionId ?: UUID.randomUUID(),
            channel = request.channel,
            currentPhase = ConversationPhase.GREETING,
            status = ConversationStatus.ACTIVE
        )
        conversationRepository.save(conversation)

        // Generate greeting using AI
        val greeting = try {
            aiAgent.generateResponse(
                ConversationPhase.GREETING,
                emptyList(),
                emptyMap(),
                "[NEW_CONVERSATION]"
            )
        } catch (e: Exception) {
            log.warn("AI greeting failed, using fallback", e)
            null
        }

        val greetingText = greeting?.message
            ?: "Welcome! I'm your digital Relationship Manager. I'm here to help you explore financial products tailored to your goals. How can I assist you today?"

        val greetingMsg = Message(
            conversationId = conversation.id,
            senderType = SenderType.AI,
            contentType = ContentType.TEXT,
            content = greetingText,
            phase = ConversationPhase.GREETING
        )
        messageRepository.save(greetingMsg)

        kafkaTemplate.send(
            KafkaTopics.CONVERSATION_EVENTS,
            conversation.id.toString(),
            ConversationStartedEvent(
                conversationId = conversation.id,
                customerId = request.customerId,
                anonymousSessionId = conversation.anonymousSessionId,
                channel = request.channel
            )
        )

        return conversation.toResponse()
    }

    @Transactional
    fun sendMessage(conversationId: UUID, request: SendMessageRequest, senderId: UUID?): Pair<MessageResponse, MessageResponse> {
        val conversation = findConversation(conversationId)
        if (conversation.status != ConversationStatus.ACTIVE) {
            throw ValidationException("Conversation is not active: ${conversation.status}")
        }

        // Save customer message
        val customerMsg = Message(
            conversationId = conversationId,
            senderType = SenderType.CUSTOMER,
            senderId = senderId,
            contentType = request.contentType,
            content = request.content,
            phase = conversation.currentPhase
        )
        messageRepository.save(customerMsg)

        // Check for handoff intent
        if (aiAgent.detectHandoffIntent(request.content)) {
            return handleHandoff(conversation, customerMsg)
        }

        // Get conversation history for context
        val history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
            .map { ChatMessage(if (it.senderType == SenderType.CUSTOMER) "Customer" else "RM", it.content) }

        val extractedData: Map<String, Any> = try {
            objectMapper.readValue(conversation.extractedData)
        } catch (e: Exception) { emptyMap() }

        // Generate AI response
        val aiResponse = aiAgent.generateResponse(
            conversation.currentPhase,
            history,
            extractedData,
            request.content
        )

        // Merge extracted entities
        val updatedData = extractedData + aiResponse.extractedEntities
        conversation.extractedData = objectMapper.writeValueAsString(updatedData)

        // Phase transition
        if (aiResponse.shouldTransitionPhase && aiResponse.suggestedNextPhase != null) {
            if (stateMachine.canTransition(conversation.currentPhase, aiResponse.suggestedNextPhase)) {
                conversation.currentPhase = aiResponse.suggestedNextPhase
                conversation.status = stateMachine.determineStatus(aiResponse.suggestedNextPhase)
            }
        }

        conversation.lastMessageAt = Instant.now()
        conversation.updatedAt = Instant.now()
        conversationRepository.save(conversation)

        // Save AI response message
        val aiMsg = Message(
            conversationId = conversationId,
            senderType = SenderType.AI,
            contentType = ContentType.TEXT,
            content = aiResponse.message,
            phase = conversation.currentPhase,
            entitiesExtracted = objectMapper.writeValueAsString(aiResponse.extractedEntities)
        )
        messageRepository.save(aiMsg)

        // If conversation completed, emit event
        if (conversation.status == ConversationStatus.COMPLETED) {
            emitCompletionEvent(conversation, updatedData)
        }

        return Pair(customerMsg.toResponse(), aiMsg.toResponse())
    }

    fun getConversationHistory(conversationId: UUID): ConversationHistoryResponse {
        val conversation = findConversation(conversationId)
        val messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
        val extractedData: Map<String, Any> = try {
            objectMapper.readValue(conversation.extractedData)
        } catch (e: Exception) { emptyMap() }

        return ConversationHistoryResponse(
            conversationId = conversation.id,
            currentPhase = conversation.currentPhase,
            status = conversation.status,
            messages = messages.map { it.toResponse() },
            extractedData = extractedData
        )
    }

    fun getCustomerConversations(customerId: UUID): List<ConversationResponse> =
        conversationRepository.findByCustomerIdOrderByLastMessageAtDesc(customerId)
            .map { it.toResponse() }

    @Transactional
    fun summarizeConversation(conversationId: UUID): ConversationSummaryResponse {
        val conversation = findConversation(conversationId)
        val messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
        val extractedData: Map<String, Any> = try {
            objectMapper.readValue(conversation.extractedData)
        } catch (e: Exception) { emptyMap() }

        val history = messages.map { ChatMessage(if (it.senderType == SenderType.CUSTOMER) "Customer" else "RM", it.content) }
        val summary = aiAgent.generateSummary(history, extractedData)

        conversation.summary = summary
        conversationRepository.save(conversation)

        val phasesCompleted = ConversationPhase.entries.filter { it.ordinal <= conversation.currentPhase.ordinal }

        return ConversationSummaryResponse(
            conversationId = conversation.id,
            summary = summary,
            phasesCompleted = phasesCompleted,
            extractedData = extractedData
        )
    }

    private fun handleHandoff(conversation: Conversation, customerMsg: Message): Pair<MessageResponse, MessageResponse> {
        conversation.status = ConversationStatus.HANDED_OFF
        conversationRepository.save(conversation)

        val aiMsg = Message(
            conversationId = conversation.id,
            senderType = SenderType.AI,
            contentType = ContentType.TEXT,
            content = "I understand you'd like to speak with a human relationship manager. Let me connect you right away. A team member will be in touch shortly. Thank you for your patience!",
            phase = conversation.currentPhase
        )
        messageRepository.save(aiMsg)

        return Pair(customerMsg.toResponse(), aiMsg.toResponse())
    }

    private fun emitCompletionEvent(conversation: Conversation, extractedData: Map<String, Any>) {
        kafkaTemplate.send(
            KafkaTopics.CONVERSATION_EVENTS,
            conversation.id.toString(),
            ConversationCompletedEvent(
                conversationId = conversation.id,
                customerId = conversation.customerId ?: UUID.randomUUID(),
                summary = conversation.summary,
                phasesCompleted = ConversationPhase.entries.toList()
            )
        )
    }

    private fun findConversation(conversationId: UUID): Conversation =
        conversationRepository.findById(conversationId).orElseThrow {
            ResourceNotFoundException("Conversation not found: $conversationId")
        }

    private fun Conversation.toResponse() = ConversationResponse(
        id, currentPhase, status, channel, createdAt.toString(), lastMessageAt.toString()
    )

    private fun Message.toResponse(): MessageResponse {
        val entities: Map<String, Any>? = entitiesExtracted?.let {
            try { objectMapper.readValue(it) } catch (e: Exception) { null }
        }
        return MessageResponse(id, conversationId, senderType, content, contentType, phase, entities, createdAt.toString())
    }
}
