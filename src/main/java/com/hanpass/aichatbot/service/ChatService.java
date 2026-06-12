package com.hanpass.aichatbot.service;

import com.hanpass.aichatbot.config.AiProperties;
import com.hanpass.aichatbot.dto.ChatRequest;
import com.hanpass.aichatbot.dto.ChatResponse;
import com.hanpass.aichatbot.dto.LlmRequest;
import com.hanpass.aichatbot.exception.ConversationNotFoundException;
import com.hanpass.aichatbot.model.ConversationEntity;
import com.hanpass.aichatbot.model.MessageEntity;
import com.hanpass.aichatbot.model.MessageRole;
import com.hanpass.aichatbot.repository.ConversationRepository;
import com.hanpass.aichatbot.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String SYSTEM_PROMPT = "Tu es un assistant technique senior Java, Spring Boot, Angular et Oracle. Réponds clairement en français.";
    private static final int HISTORY_LIMIT = 20;

    private final LlmClientService llmClientService;
    private final AiProperties aiProperties;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(
            LlmClientService llmClientService,
            AiProperties aiProperties,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this.llmClientService = llmClientService;
        this.aiProperties = aiProperties;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatResponse ask(ChatRequest chatRequest) {
        String userMessage = chatRequest.message().trim();
        ConversationEntity conversation = findOrCreateConversation(chatRequest.conversationId(), userMessage);
        log.info("Preparing chat completion request for conversation {}", conversation.getId());

        LlmRequest request = new LlmRequest(aiProperties.model(), buildLlmMessages(conversation, userMessage));
        messageRepository.save(new MessageEntity(conversation, MessageRole.USER, userMessage));

        String answer = llmClientService.complete(request);
        messageRepository.save(new MessageEntity(conversation, MessageRole.ASSISTANT, answer));
        conversation.touch();
        conversationRepository.save(conversation);

        return new ChatResponse(conversation.getId(), answer);
    }

    private ConversationEntity findOrCreateConversation(UUID conversationId, String userMessage) {
        if (conversationId == null) {
            ConversationEntity conversation = new ConversationEntity(generateTitle(userMessage));
            ConversationEntity savedConversation = conversationRepository.save(conversation);
            log.info("Created conversation {}", savedConversation.getId());
            return savedConversation;
        }

        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    }

    private List<LlmRequest.Message> buildLlmMessages(ConversationEntity conversation, String currentUserMessage) {
        List<MessageEntity> history = messageRepository.findByConversationOrderByCreatedAtDesc(
                        conversation,
                PageRequest.of(0, HISTORY_LIMIT)
                ).stream()
                .sorted(Comparator.comparing(MessageEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<LlmRequest.Message> messages = new ArrayList<>();
        messages.add(new LlmRequest.Message("system", SYSTEM_PROMPT));
        history.stream()
                .map(this::toLlmMessage)
                .forEach(messages::add);
        messages.add(new LlmRequest.Message("user", currentUserMessage));
        return messages;
    }

    private LlmRequest.Message toLlmMessage(MessageEntity message) {
        return new LlmRequest.Message(toLlmRole(message.getRole()), message.getContent());
    }

    private String toLlmRole(MessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
        };
    }

    private String generateTitle(String userMessage) {
        String normalized = userMessage.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 60) {
            return normalized;
        }
        return normalized.substring(0, 57) + "...";
    }
}
