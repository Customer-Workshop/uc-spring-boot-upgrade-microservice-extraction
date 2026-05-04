package com.bank.rm.conversation.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.conversation.dto.*
import com.bank.rm.conversation.service.ConversationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/conversations")
class ConversationController(private val conversationService: ConversationService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun startConversation(@Valid @RequestBody request: StartConversationRequest): ApiResponse<ConversationResponse> =
        ApiResponse(success = true, data = conversationService.startConversation(request))

    @PostMapping("/{conversationId}/messages")
    fun sendMessage(
        @PathVariable conversationId: UUID,
        @Valid @RequestBody request: SendMessageRequest,
        authentication: Authentication?
    ): ApiResponse<Map<String, MessageResponse>> {
        val senderId = authentication?.name?.let { UUID.fromString(it) }
        val (customerMsg, aiMsg) = conversationService.sendMessage(conversationId, request, senderId)
        return ApiResponse(
            success = true,
            data = mapOf("customerMessage" to customerMsg, "aiResponse" to aiMsg)
        )
    }

    @GetMapping("/{conversationId}")
    fun getConversationHistory(@PathVariable conversationId: UUID): ApiResponse<ConversationHistoryResponse> =
        ApiResponse(success = true, data = conversationService.getConversationHistory(conversationId))

    @GetMapping("/customer/{customerId}")
    fun getCustomerConversations(@PathVariable customerId: UUID): ApiResponse<List<ConversationResponse>> =
        ApiResponse(success = true, data = conversationService.getCustomerConversations(customerId))

    @PostMapping("/{conversationId}/summarize")
    fun summarize(@PathVariable conversationId: UUID): ApiResponse<ConversationSummaryResponse> =
        ApiResponse(success = true, data = conversationService.summarizeConversation(conversationId))
}
