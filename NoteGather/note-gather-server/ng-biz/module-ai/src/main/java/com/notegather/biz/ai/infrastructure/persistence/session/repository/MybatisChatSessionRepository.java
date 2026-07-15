package com.notegather.biz.ai.infrastructure.persistence.session.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.notegather.biz.ai.domain.model.ChatSession;
import com.notegather.biz.ai.domain.repository.ChatSessionRepository;
import com.notegather.biz.ai.infrastructure.persistence.session.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MybatisChatSessionRepository implements ChatSessionRepository {

    private final ChatSessionMapper chatSessionMapper;

    @Override
    public void save(ChatSession session) {
        chatSessionMapper.insert(session);
    }

    @Override
    public ChatSession lockByIdAndUserId(Long sessionId, Long userId) {
        return chatSessionMapper.selectForUpdate(sessionId, userId);
    }

    @Override
    public void touch(Long sessionId, Long userId) {
        ChatSession update = new ChatSession();
        update.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.update(update, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getUserId, userId));
    }
}
