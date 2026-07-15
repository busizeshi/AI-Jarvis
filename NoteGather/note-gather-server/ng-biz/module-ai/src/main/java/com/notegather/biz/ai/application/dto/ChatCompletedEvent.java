package com.notegather.biz.ai.application.dto;

import java.util.List;

public record ChatCompletedEvent(
        Long sessionId,
        Long messageId,
        List<ChatCitationData> citations
) {
}
