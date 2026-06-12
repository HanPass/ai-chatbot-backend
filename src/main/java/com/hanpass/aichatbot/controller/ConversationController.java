package com.hanpass.aichatbot.controller;

import com.hanpass.aichatbot.dto.ConversationSummaryResponse;
import com.hanpass.aichatbot.dto.MessageResponse;
import com.hanpass.aichatbot.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationSummaryResponse> listConversations() {
        return conversationService.listConversations();
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> listMessages(@PathVariable UUID id) {
        return conversationService.listMessages(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
