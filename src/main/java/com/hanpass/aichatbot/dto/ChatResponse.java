package com.hanpass.aichatbot.dto;

import java.util.UUID;

public record ChatResponse(UUID conversationId, String answer) {
}
