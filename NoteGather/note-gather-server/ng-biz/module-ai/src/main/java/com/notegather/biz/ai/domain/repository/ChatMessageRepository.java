package com.notegather.biz.ai.domain.repository;

import com.notegather.biz.ai.domain.model.ChatMessage;

public interface ChatMessageRepository {

    void save(ChatMessage message);

    int currentMessageNo(Long sessionId);

    void completeAssistant(Long messageId, Long sessionId, String content);

    void failAssistant(Long messageId, Long sessionId, String content, String errorMessage);
}
