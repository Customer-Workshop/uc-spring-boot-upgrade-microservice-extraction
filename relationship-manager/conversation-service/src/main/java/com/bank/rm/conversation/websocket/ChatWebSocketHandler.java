package com.bank.rm.conversation.websocket;

import com.bank.rm.conversation.dto.ConversationDtos.SendMessageRequest;
import com.bank.rm.conversation.dto.ConversationDtos.SendMessageResponse;
import com.bank.rm.conversation.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.UUID;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ConversationService conversationService, ObjectMapper objectMapper) {
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String conversationId = extractConversationId(session);
        if (conversationId == null) {
            session.sendMessage(new TextMessage("{\"error\":\"Missing conversationId in path\"}"));
            return;
        }
        try {
            SendMessageRequest request = new SendMessageRequest(message.getPayload());
            SendMessageResponse response = conversationService.sendMessage(
                    UUID.fromString(conversationId), request);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("WebSocket message handling error: {}", e.getMessage());
            session.sendMessage(new TextMessage("{\"error\":\"" + e.getMessage() + "\"}"));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket disconnected: {} status={}", session.getId(), status);
    }

    private String extractConversationId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }
}
