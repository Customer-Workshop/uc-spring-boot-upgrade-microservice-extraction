package com.bank.rm.conversation.dto;

import com.bank.rm.common.dto.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConversationDtos {

    public record StartConversationRequest(CommunicationChannel channel, UUID customerId, UUID anonymousSessionId) {}

    public record SendMessageRequest(String content) {}

    public record ConversationResponse(UUID conversationId, ConversationPhase currentPhase,
                                        ConversationStatus status, CommunicationChannel channel,
                                        Instant createdAt, Instant lastMessageAt) {}

    public record MessageResponse(UUID messageId, UUID conversationId, SenderType senderType,
                                   String content, ContentType contentType, ConversationPhase phase,
                                   Map<String, Object> entitiesExtracted, Instant createdAt) {}

    public record SendMessageResponse(MessageResponse customerMessage, MessageResponse aiResponse) {}

    public record ConversationDetailResponse(UUID conversationId, ConversationPhase currentPhase,
                                              ConversationStatus status, List<MessageResponse> messages,
                                              Map<String, Object> extractedData) {}

    private ConversationDtos() {}
}
