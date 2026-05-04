package com.bank.rm.conversation.service;

import com.bank.rm.common.dto.*;
import com.bank.rm.common.event.*;
import com.bank.rm.common.exception.ResourceNotFoundException;
import com.bank.rm.conversation.ai.ConversationAIAgent;
import com.bank.rm.conversation.ai.EntityExtractor;
import com.bank.rm.conversation.domain.Conversation;
import com.bank.rm.conversation.domain.Message;
import com.bank.rm.conversation.dto.ConversationDtos.*;
import com.bank.rm.conversation.engine.ConversationStateMachine;
import com.bank.rm.conversation.repository.ConversationRepository;
import com.bank.rm.conversation.repository.MessageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationAIAgent aiAgent;
    private final EntityExtractor entityExtractor;
    private final ConversationStateMachine stateMachine;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ConversationService(ConversationRepository conversationRepository,
                                MessageRepository messageRepository,
                                ConversationAIAgent aiAgent,
                                EntityExtractor entityExtractor,
                                ConversationStateMachine stateMachine,
                                KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiAgent = aiAgent;
        this.entityExtractor = entityExtractor;
        this.stateMachine = stateMachine;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConversationResponse startConversation(StartConversationRequest request) {
        UUID conversationId = UUID.randomUUID();
        CommunicationChannel channel = request.channel() != null ? request.channel() : CommunicationChannel.WEB;
        Conversation conversation = new Conversation(
                conversationId, request.customerId(), request.anonymousSessionId(), channel);
        conversationRepository.save(conversation);

        String greeting = aiAgent.generateResponse("", ConversationPhase.GREETING, Map.of(), "");
        Message greetingMsg = new Message(UUID.randomUUID(), conversationId, SenderType.AI, null,
                ContentType.TEXT, greeting, ConversationPhase.GREETING);
        messageRepository.save(greetingMsg);

        publishEvent(KafkaTopics.CONVERSATION_EVENTS,
                new ConversationStartedEvent(conversationId, request.customerId(),
                        request.anonymousSessionId(), channel));

        return new ConversationResponse(conversationId, conversation.getCurrentPhase(),
                conversation.getStatus(), channel, conversation.getCreatedAt(), Instant.now());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public SendMessageResponse sendMessage(UUID conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        Message customerMsg = new Message(UUID.randomUUID(), conversationId, SenderType.CUSTOMER,
                conversation.getCustomerId(), ContentType.TEXT, request.content(), conversation.getCurrentPhase());
        messageRepository.save(customerMsg);

        Map<String, Object> existingData = parseJson(conversation.getExtractedData());
        Map<String, Object> updatedData = entityExtractor.extract(
                request.content(), conversation.getCurrentPhase(), existingData);
        conversation.setExtractedData(toJson(updatedData));

        if (stateMachine.canTransition(conversation.getCurrentPhase(), updatedData)) {
            ConversationPhase nextPhase = stateMachine.getNextPhase(conversation.getCurrentPhase());
            conversation.setCurrentPhase(nextPhase);
            if (nextPhase == ConversationPhase.COMPLETED) {
                conversation.setStatus(ConversationStatus.COMPLETED);
                String summary = aiAgent.generateSummary(getHistory(conversationId), updatedData);
                conversation.setSummary(summary);
                publishEvent(KafkaTopics.CONVERSATION_EVENTS,
                        new ConversationCompletedEvent(conversationId, conversation.getCustomerId(),
                                summary, List.of(ConversationPhase.values())));
            }
        }

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        String history = getHistory(conversationId);
        String aiResponse = aiAgent.generateResponse(request.content(), conversation.getCurrentPhase(),
                updatedData, history);
        Message aiMsg = new Message(UUID.randomUUID(), conversationId, SenderType.AI, null,
                ContentType.TEXT, aiResponse, conversation.getCurrentPhase());
        messageRepository.save(aiMsg);

        return new SendMessageResponse(toMessageResponse(customerMsg), toMessageResponse(aiMsg));
    }

    public ConversationDetailResponse getConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<MessageResponse> messageResponses = messages.stream()
                .map(this::toMessageResponse).collect(Collectors.toList());
        Map<String, Object> extractedData = parseJson(conversation.getExtractedData());
        return new ConversationDetailResponse(conversationId, conversation.getCurrentPhase(),
                conversation.getStatus(), messageResponses, extractedData);
    }

    private String getHistory(UUID conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        StringBuilder sb = new StringBuilder();
        for (Message m : messages) {
            sb.append(m.getSenderType() == SenderType.CUSTOMER ? "Customer: " : "RM: ")
              .append(m.getContent()).append("\n");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private MessageResponse toMessageResponse(Message m) {
        Map<String, Object> entities = parseJson(m.getEntitiesExtracted());
        return new MessageResponse(m.getId(), m.getConversationId(), m.getSenderType(),
                m.getContent(), m.getContentType(), m.getPhase(), entities, m.getCreatedAt());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null || json.isEmpty()) return new HashMap<>();
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void publishEvent(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, toJson(event));
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", topic, e.getMessage());
        }
    }
}
