package com.notegather.biz.ai.domain.repository;

import com.notegather.biz.ai.domain.model.ChatSession;

public interface ChatSessionRepository {

    void save(ChatSession session);

    ChatSession lockByIdAndUserId(Long sessionId, Long userId);

    void touch(Long sessionId, Long userId);
}
