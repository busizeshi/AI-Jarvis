package com.notegather.biz.ai.infrastructure.persistence.message.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.notegather.biz.ai.domain.enums.ChatMessageRole;
import com.notegather.biz.ai.domain.enums.ChatMessageStatus;
import com.notegather.biz.ai.domain.model.ChatMessage;
import com.notegather.biz.ai.domain.repository.ChatMessageRepository;
import com.notegather.biz.ai.infrastructure.persistence.message.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisChatMessageRepository implements ChatMessageRepository {

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public void save(ChatMessage message) {
        chatMessageMapper.insert(message);
    }

    @Override
    public int currentMessageNo(Long sessionId) {
        Integer messageNo = chatMessageMapper.selectCurrentMessageNo(sessionId);
        return messageNo == null ? 0 : messageNo;
    }

    @Override
    public void completeAssistant(Long messageId, Long sessionId, String content) {
        updateAssistant(messageId, sessionId, content, ChatMessageStatus.COMPLETED, null);
    }

    @Override
    public void failAssistant(Long messageId, Long sessionId, String content, String errorMessage) {
        updateAssistant(messageId, sessionId, content, ChatMessageStatus.FAILED, errorMessage);
    }

    private void updateAssistant(
            Long messageId,
            Long sessionId,
            String content,
            ChatMessageStatus status,
            String errorMessage
    ) {
        ChatMessage update = new ChatMessage();
        update.setContent(content);
        update.setStatus(status.name());
        update.setErrorMsg(errorMessage);
        chatMessageMapper.update(update, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getId, messageId)
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getRole, ChatMessageRole.ASSISTANT.name())
                .eq(ChatMessage::getStatus, ChatMessageStatus.STREAMING.name()));
    }
}
