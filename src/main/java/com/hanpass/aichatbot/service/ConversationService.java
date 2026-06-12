package com.hanpass.aichatbot.service;

import com.hanpass.aichatbot.dto.ConversationSummaryResponse;
import com.hanpass.aichatbot.dto.MessageResponse;
import com.hanpass.aichatbot.exception.ConversationNotFoundException;
import com.hanpass.aichatbot.model.ConversationEntity;
import com.hanpass.aichatbot.model.MessageEntity;
import com.hanpass.aichatbot.repository.ConversationRepository;
import com.hanpass.aichatbot.repository.MessageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listConversations() {
        return conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(UUID conversationId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public void deleteConversation(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException(conversationId);
        }
        conversationRepository.deleteById(conversationId);
    }

    private ConversationSummaryResponse toSummary(ConversationEntity conversation) {
        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private MessageResponse toMessageResponse(MessageEntity message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
