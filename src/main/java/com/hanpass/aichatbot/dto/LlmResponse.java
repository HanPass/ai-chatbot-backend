package com.hanpass.aichatbot.dto;

import java.util.List;

public record LlmResponse(List<Choice> choices) {

    public record Choice(Message message) {
    }

    public record Message(String role, String content) {
    }
}
