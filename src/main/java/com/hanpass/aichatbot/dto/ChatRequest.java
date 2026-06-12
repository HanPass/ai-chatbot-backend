package com.hanpass.aichatbot.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ChatRequest(UUID conversationId, @NotBlank String message) {
}
