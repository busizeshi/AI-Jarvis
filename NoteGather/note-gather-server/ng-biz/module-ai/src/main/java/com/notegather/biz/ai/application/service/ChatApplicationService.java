package com.notegather.biz.ai.application.service;

import com.notegather.biz.ai.application.dto.ChatCitationData;
import com.notegather.biz.ai.application.dto.ChatCompletedEvent;
import com.notegather.biz.ai.application.dto.ChatFailedEvent;
import com.notegather.biz.ai.application.dto.ChatRetrievalMetadata;
import com.notegather.biz.ai.application.dto.ChatStart;
import com.notegather.biz.ai.application.dto.ChatStreamRequest;
import com.notegather.biz.ai.application.dto.ChatTokenEvent;
import com.notegather.biz.ai.infrastructure.config.ChatFeatureProperties;
import com.notegather.biz.ai.infrastructure.http.AiChatHttpClient;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatApplicationService {

    private static final String CLIENT_DISCONNECTED = "客户端连接已断开";
    private static final String CLIENT_TIMEOUT = "客户端连接已超时";
    private static final String RESPONSE_FAILED = "回答生成失败，请稍后重试";

    private final ChatPersistenceService chatPersistenceService;
    private final AiChatHttpClient aiChatHttpClient;
    private final ChatFeatureProperties chatFeatureProperties;

    public SseEmitter stream(ChatStreamRequest request) {
        if (!chatFeatureProperties.isEnabled()) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "问答能力当前处于灰度关闭状态");
        }
        ChatStart chat = chatPersistenceService.prepare(UserContext.getUserId(), request);
        ChatStreamState state = new ChatStreamState(chat);
        state.emitter().onCompletion(() -> fail(state, CLIENT_DISCONNECTED, true));
        state.emitter().onTimeout(() -> fail(state, CLIENT_TIMEOUT, true));
        state.emitter().onError(error -> fail(state, error.getMessage(), true));
        state.setRequest(aiChatHttpClient.stream(chat, new AiChatHttpClient.Listener() {
            @Override
            public void onToken(String token) {
                if (!state.isTerminal() && !token.isBlank()) {
                    state.append(token);
                    send(state, "token", new ChatTokenEvent(token));
                }
            }

            @Override
            public void onDone(List<ChatCitationData> citations, ChatRetrievalMetadata retrieval) {
                complete(state, citations, retrieval);
            }

            @Override
            public void onError(Throwable error) {
                log.warn("AI HTTP stream failed sessionId={} messageId={}", chat.sessionId(), chat.assistantMessageId(), error);
                fail(state, error.getMessage(), false);
            }
        }));
        return state.emitter();
    }

    private void complete(ChatStreamState state, List<ChatCitationData> citations, ChatRetrievalMetadata retrieval) {
        if (!state.markTerminal()) {
            return;
        }
        try {
            chatPersistenceService.complete(state.chat(), state.content(), citations);
            sendCompleted(state, new ChatCompletedEvent(state.chat().sessionId(), state.chat().assistantMessageId(), citations, retrieval));
        } catch (RuntimeException error) {
            log.error("Persisting AI response failed sessionId={} messageId={}", state.chat().sessionId(), state.chat().assistantMessageId(), error);
            chatPersistenceService.fail(state.chat(), state.content(), error.getMessage());
            sendFailure(state);
        }
    }

    private void fail(ChatStreamState state, String errorMessage, boolean clientDisconnected) {
        if (!state.markTerminal()) {
            return;
        }
        state.cancel();
        String message = errorMessage == null || errorMessage.isBlank() ? RESPONSE_FAILED : errorMessage;
        try {
            chatPersistenceService.fail(state.chat(), state.content(), message);
        } catch (RuntimeException persistenceError) {
            log.error("Persisting AI failure failed sessionId={} messageId={}", state.chat().sessionId(), state.chat().assistantMessageId(), persistenceError);
        }
        if (!clientDisconnected) {
            sendFailure(state);
        }
    }

    private void send(ChatStreamState state, String eventName, Object eventData) {
        try {
            state.emitter().send(SseEmitter.event().name(eventName).data(eventData));
        } catch (IOException | IllegalStateException error) {
            fail(state, CLIENT_DISCONNECTED, true);
        }
    }

    private void sendCompleted(ChatStreamState state, ChatCompletedEvent event) {
        try {
            state.emitter().send(SseEmitter.event().name("done").data(event));
            state.emitter().complete();
        } catch (IOException | IllegalStateException error) {
            log.debug("Client disconnected before completion event messageId={}", state.chat().assistantMessageId());
        }
    }

    private void sendFailure(ChatStreamState state) {
        try {
            state.emitter().send(SseEmitter.event().name("error").data(new ChatFailedEvent(RESPONSE_FAILED)));
            state.emitter().complete();
        } catch (IOException | IllegalStateException ignored) {
            log.debug("Client disconnected before failure event messageId={}", state.chat().assistantMessageId());
        }
    }

    private static final class ChatStreamState {
        private final ChatStart chat;
        private final SseEmitter emitter = new SseEmitter(0L);
        private final AtomicBoolean terminal = new AtomicBoolean();
        private final AtomicReference<CompletableFuture<Void>> request = new AtomicReference<>();
        private final StringBuilder content = new StringBuilder();

        private ChatStreamState(ChatStart chat) {
            this.chat = chat;
        }

        private ChatStart chat() { return chat; }
        private SseEmitter emitter() { return emitter; }
        private boolean isTerminal() { return terminal.get(); }
        private boolean markTerminal() { return terminal.compareAndSet(false, true); }
        private synchronized void append(String token) { content.append(token); }
        private synchronized String content() { return content.toString(); }

        private void setRequest(CompletableFuture<Void> future) {
            request.set(future);
            if (isTerminal()) {
                future.cancel(true);
            }
        }

        private void cancel() {
            CompletableFuture<Void> future = request.get();
            if (future != null) {
                future.cancel(true);
            }
        }
    }
}
