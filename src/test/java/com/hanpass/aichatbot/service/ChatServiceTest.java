package com.hanpass.aichatbot.service;

import com.hanpass.aichatbot.config.AiProperties;
import com.hanpass.aichatbot.dto.ChatResponse;
import com.hanpass.aichatbot.dto.LlmRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private LlmClientService llmClientService;

    @Test
    void askBuildsPromptAndReturnsAnswer() {
        AiProperties aiProperties = new AiProperties("https://api.openai.com/v1", "test-key", "gpt-4.1-mini", 60);
        ChatService chatService = new ChatService(llmClientService, aiProperties);
        when(llmClientService.complete(org.mockito.ArgumentMatchers.any())).thenReturn("Une NullPointerException arrive lorsque...");

        ChatResponse response = chatService.ask("Explique-moi une NullPointerException en Java");

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmClientService).complete(captor.capture());

        assertThat(response.answer()).isEqualTo("Une NullPointerException arrive lorsque...");
        assertThat(captor.getValue().model()).isEqualTo("gpt-4.1-mini");
        assertThat(captor.getValue().messages()).hasSize(2);
        assertThat(captor.getValue().messages().get(0).role()).isEqualTo("system");
        assertThat(captor.getValue().messages().get(1).role()).isEqualTo("user");
        assertThat(captor.getValue().messages().get(1).content()).isEqualTo("Explique-moi une NullPointerException en Java");
    }
}
