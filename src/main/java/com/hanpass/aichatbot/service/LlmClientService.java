package com.hanpass.aichatbot.service;

import com.hanpass.aichatbot.dto.LlmRequest;
import com.hanpass.aichatbot.dto.LlmResponse;
import com.hanpass.aichatbot.exception.LlmUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class LlmClientService {

    private static final Logger log = LoggerFactory.getLogger(LlmClientService.class);

    private final WebClient llmWebClient;

    public LlmClientService(WebClient llmWebClient) {
        this.llmWebClient = llmWebClient;
    }

    public String complete(LlmRequest request) {
        log.info("Calling LLM provider with model {}", request.model());

        LlmResponse response = llmWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("No error body")
                                .flatMap(errorBody -> {
                                    log.warn("LLM provider returned HTTP {}: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new LlmUnavailableException("LLM provider returned an error"));
                                }))
                .bodyToMono(LlmResponse.class)
                .onErrorMap(exception -> exception instanceof LlmUnavailableException
                        ? exception
                        : new LlmUnavailableException("Unable to call LLM provider", exception))
                .block();

        return Optional.ofNullable(response)
                .map(LlmResponse::choices)
                .orElse(List.of())
                .stream()
                .findFirst()
                .map(LlmResponse.Choice::message)
                .map(LlmResponse.Message::content)
                .filter(content -> !content.isBlank())
                .orElseThrow(() -> new LlmUnavailableException("LLM provider returned an empty response"));
    }
}
