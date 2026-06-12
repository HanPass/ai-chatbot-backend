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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private LlmClientService llmClientService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        AiProperties aiProperties = new AiProperties("https://api.openai.com/v1", "test-key", "gpt-4.1-mini", 60);
        chatService = new ChatService(llmClientService, aiProperties, conversationRepository, messageRepository);
    }

    @Test
    void askCreatesConversationWhenConversationIdIsMissing() {
        when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(any(), any(Pageable.class))).thenReturn(List.of());
        when(llmClientService.complete(any())).thenReturn("Une NullPointerException arrive lorsque...");

        ChatResponse response = chatService.ask(new ChatRequest(null, "Explique-moi une NullPointerException en Java"));

        assertThat(response.conversationId()).isNotNull();
        assertThat(response.answer()).isEqualTo("Une NullPointerException arrive lorsque...");
        verify(conversationRepository, times(2)).save(any(ConversationEntity.class));
        verify(messageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    void askAddsMessageToExistingConversation() {
        ConversationEntity conversation = new ConversationEntity("NullPointerException");
        UUID conversationId = conversation.getId();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(any(), any(Pageable.class))).thenReturn(List.of(
                new MessageEntity(conversation, MessageRole.USER, "Explique-moi NullPointerException"),
                new MessageEntity(conversation, MessageRole.ASSISTANT, "C'est une exception Java.")
        ));
        when(llmClientService.complete(any())).thenReturn("Voici un exemple simple.");

        ChatResponse response = chatService.ask(new ChatRequest(conversationId, "Donne-moi un exemple"));

        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.answer()).isEqualTo("Voici un exemple simple.");
        verify(messageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    void askFailsWhenConversationDoesNotExist() {
        UUID conversationId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.ask(new ChatRequest(conversationId, "Bonjour")))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void askSendsOnlyLimitedHistoryToLlm() {
        ConversationEntity conversation = new ConversationEntity("Historique");
        when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(any(), any(Pageable.class))).thenReturn(List.of());
        when(llmClientService.complete(any())).thenReturn("Réponse");

        chatService.ask(new ChatRequest(null, "Question"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findByConversationOrderByCreatedAtDesc(any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void askBuildsPromptWithSystemHistoryAndCurrentMessage() {
        ConversationEntity conversation = new ConversationEntity("NullPointerException");
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(any(), any(Pageable.class))).thenReturn(List.of(
                new MessageEntity(conversation, MessageRole.USER, "Explique-moi NullPointerException"),
                new MessageEntity(conversation, MessageRole.ASSISTANT, "C'est une exception Java.")
        ));
        when(llmClientService.complete(any())).thenReturn("Voici un exemple.");

        chatService.ask(new ChatRequest(conversation.getId(), "Donne-moi un exemple"));

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmClientService).complete(captor.capture());
        assertThat(captor.getValue().messages())
                .extracting(LlmRequest.Message::role)
                .containsExactly("system", "user", "assistant", "user");
    }
}
