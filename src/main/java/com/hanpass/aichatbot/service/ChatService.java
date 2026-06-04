package com.hanpass.aichatbot.service;

import com.hanpass.aichatbot.config.AiProperties;
import com.hanpass.aichatbot.dto.ChatResponse;
import com.hanpass.aichatbot.dto.LlmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String SYSTEM_PROMPT = "Tu es un assistant technique senior Java, Spring Boot, Angular et Oracle. Réponds clairement en français.";

    private final LlmClientService llmClientService;
    private final AiProperties aiProperties;

    public ChatService(LlmClientService llmClientService, AiProperties aiProperties) {
        this.llmClientService = llmClientService;
        this.aiProperties = aiProperties;
    }

    public ChatResponse ask(String userMessage) {
        log.info("Preparing chat completion request");

        LlmRequest request = new LlmRequest(
                aiProperties.model(),
                List.of(
                        new LlmRequest.Message("system", SYSTEM_PROMPT),
                        new LlmRequest.Message("user", userMessage)
                )
        );

        String answer = llmClientService.complete(request);
        return new ChatResponse(answer);
    }
}
