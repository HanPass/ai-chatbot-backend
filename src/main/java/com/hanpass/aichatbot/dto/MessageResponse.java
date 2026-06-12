package com.hanpass.aichatbot.dto;

import com.hanpass.aichatbot.model.MessageRole;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id, MessageRole role, String content, Instant createdAt) {
}
