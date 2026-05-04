package com.bank.rm.conversation.websocket

import com.bank.rm.common.dto.ContentType
import com.bank.rm.conversation.dto.SendMessageRequest
import com.bank.rm.conversation.service.ConversationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val conversationService: ConversationService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val conversationId = extractConversationId(session)
        if (conversationId != null) {
            sessions[conversationId] = session
            log.info("WebSocket connected for conversation: $conversationId")
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload: Map<String, String> = objectMapper.readValue(message.payload)
            val conversationId = payload["conversationId"] ?: extractConversationId(session)
            val content = payload["content"] ?: return

            if (conversationId == null) {
                session.sendMessage(TextMessage(objectMapper.writeValueAsString(
                    mapOf("error" to "No conversation ID")
                )))
                return
            }

            val request = SendMessageRequest(content = content, contentType = ContentType.TEXT)
            val senderId = payload["senderId"]?.let { UUID.fromString(it) }
            val (customerMsg, aiMsg) = conversationService.sendMessage(
                UUID.fromString(conversationId), request, senderId
            )

            val response = objectMapper.writeValueAsString(mapOf(
                "type" to "message",
                "customerMessage" to customerMsg,
                "aiResponse" to aiMsg
            ))
            session.sendMessage(TextMessage(response))
        } catch (e: Exception) {
            log.error("WebSocket message handling error", e)
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(
                mapOf("type" to "error", "message" to "Failed to process message: ${e.message}")
            )))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val conversationId = extractConversationId(session)
        if (conversationId != null) {
            sessions.remove(conversationId)
            log.info("WebSocket disconnected for conversation: $conversationId")
        }
    }

    private fun extractConversationId(session: WebSocketSession): String? {
        val path = session.uri?.path ?: return null
        val regex = Regex("/ws/chat/([a-f0-9-]+)")
        return regex.find(path)?.groupValues?.get(1)
    }
}
