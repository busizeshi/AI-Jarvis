package com.notegather.biz.ai.application.service;

import com.notegather.biz.ai.application.dto.ChatCitationData;
import com.notegather.biz.ai.application.dto.ChatStart;
import com.notegather.biz.ai.application.dto.ChatStreamRequest;
import com.notegather.biz.ai.domain.enums.ChatMessageRole;
import com.notegather.biz.ai.domain.enums.ChatMessageStatus;
import com.notegather.biz.ai.domain.model.ChatCitation;
import com.notegather.biz.ai.domain.model.ChatMessage;
import com.notegather.biz.ai.domain.model.ChatSession;
import com.notegather.biz.ai.domain.repository.ChatCitationRepository;
import com.notegather.biz.ai.domain.repository.ChatMessageRepository;
import com.notegather.biz.ai.domain.repository.ChatSessionRepository;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatPersistenceService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCitationRepository chatCitationRepository;

    @Transactional(rollbackFor = Exception.class)
    public ChatStart prepare(Long userId, ChatStreamRequest request) {
        ChatSession session = resolveSession(userId, request.sessionId());
        int nextMessageNo = chatMessageRepository.currentMessageNo(session.getId()) + 1;
        ChatMessage userMessage = createMessage(
                session.getId(), nextMessageNo, ChatMessageRole.USER, request.question(), ChatMessageStatus.COMPLETED
        );
        ChatMessage assistantMessage = createMessage(
                session.getId(), nextMessageNo + 1, ChatMessageRole.ASSISTANT, "", ChatMessageStatus.STREAMING
        );
        chatMessageRepository.save(userMessage);
        chatMessageRepository.save(assistantMessage);
        chatSessionRepository.touch(session.getId(), userId);
        return new ChatStart(session.getId(), assistantMessage.getId(), userId, request.question());
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(ChatStart chat, String content, List<ChatCitationData> citations) {
        chatMessageRepository.completeAssistant(chat.assistantMessageId(), chat.sessionId(), content);
        for (int index = 0; index < citations.size(); index++) {
            chatCitationRepository.save(toCitation(chat.assistantMessageId(), index, citations.get(index)));
        }
        chatSessionRepository.touch(chat.sessionId(), chat.userId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void fail(ChatStart chat, String content, String errorMessage) {
        chatMessageRepository.failAssistant(chat.assistantMessageId(), chat.sessionId(), content, errorMessage);
        chatSessionRepository.touch(chat.sessionId(), chat.userId());
    }

    private ChatSession resolveSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            chatSessionRepository.save(session);
            return session;
        }
        ChatSession session = chatSessionRepository.lockByIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在或无权访问");
        }
        return session;
    }

    private ChatMessage createMessage(
            Long sessionId,
            int messageNo,
            ChatMessageRole role,
            String content,
            ChatMessageStatus status
    ) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setMessageNo(messageNo);
        message.setRole(role.name());
        message.setContent(content);
        message.setStatus(status.name());
        return message;
    }

    private ChatCitation toCitation(Long messageId, int order, ChatCitationData source) {
        ChatCitation citation = new ChatCitation();
        citation.setMessageId(messageId);
        citation.setNoteId(source.noteId());
        citation.setCitationOrder(order);
        citation.setNoteTitle(source.noteTitle());
        citation.setChunkText(source.chunkText());
        citation.setScore(source.score());
        return citation;
    }
}
