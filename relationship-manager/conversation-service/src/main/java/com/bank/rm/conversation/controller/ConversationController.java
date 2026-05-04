package com.bank.rm.conversation.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.conversation.dto.ConversationDtos.*;
import com.bank.rm.conversation.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @RequestBody StartConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(conversationService.startConversation(request)));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @PathVariable UUID conversationId, @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.sendMessage(conversationId, request)));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getConversation(
            @PathVariable UUID conversationId) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.getConversation(conversationId)));
    }
}
