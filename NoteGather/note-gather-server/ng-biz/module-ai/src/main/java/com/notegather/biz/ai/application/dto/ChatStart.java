package com.notegather.biz.ai.application.dto;

public record ChatStart(
        Long sessionId,
        Long assistantMessageId,
        Long userId,
        String question
) {
}
