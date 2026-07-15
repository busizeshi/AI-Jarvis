package com.notegather.biz.ai.application.service;

import com.notegather.biz.ai.application.dto.ChatCitationData;
import com.notegather.biz.ai.application.dto.ChatCompletedEvent;
import com.notegather.biz.ai.application.dto.ChatFailedEvent;
import com.notegather.biz.ai.application.dto.ChatStart;
import com.notegather.biz.ai.application.dto.ChatStreamRequest;
import com.notegather.biz.ai.application.dto.ChatTokenEvent;
import com.notegather.biz.ai.infrastructure.grpc.AiChatGrpcClient;
import com.notegather.common.security.context.UserContext;
import com.notegather.grpc.ai.ChatChunk;
import com.notegather.grpc.ai.Citation;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatApplicationService {

    private static final String CLIENT_DISCONNECTED = "客户端连接已断开";
    private static final String CLIENT_TIMEOUT = "客户端连接已超时";
    private static final String GRPC_COMPLETED_WITHOUT_RESULT = "AI 服务未返回完成标记";
    private static final String RESPONSE_FAILED = "回答生成失败，请稍后重试";

    private final ChatPersistenceService chatPersistenceService;
    private final AiChatGrpcClient aiChatGrpcClient;

    public SseEmitter stream(ChatStreamRequest request) {
        ChatStart chat = chatPersistenceService.prepare(UserContext.getUserId(), request);
        ChatStreamState state = new ChatStreamState(chat);
        state.emitter().onCompletion(() -> fail(state, CLIENT_DISCONNECTED, true));
        state.emitter().onTimeout(() -> fail(state, CLIENT_TIMEOUT, true));
        state.emitter().onError(error -> fail(state, error.getMessage(), true));
        try {
            state.setCancellation(aiChatGrpcClient.stream(chat, observer(state)));
        } catch (RuntimeException error) {
            log.error("启动 AI gRPC 调用失败 sessionId={} messageId={}", chat.sessionId(), chat.assistantMessageId(), error);
            fail(state, error.getMessage(), false);
        }
        return state.emitter();
    }

    private StreamObserver<ChatChunk> observer(ChatStreamState state) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ChatChunk chunk) {
                if (state.isTerminal()) {
                    return;
                }
                if (chunk.getIsDone()) {
                    complete(state, citations(chunk));
                    return;
                }
                if (!chunk.getContent().isEmpty()) {
                    state.append(chunk.getContent());
                    send(state, "token", new ChatTokenEvent(chunk.getContent()));
                }
            }

            @Override
            public void onError(Throwable error) {
                log.warn("AI gRPC 流调用失败 sessionId={} messageId={}",
                        state.chat().sessionId(), state.chat().assistantMessageId(), error);
                fail(state, error.getMessage(), false);
            }

            @Override
            public void onCompleted() {
                fail(state, GRPC_COMPLETED_WITHOUT_RESULT, false);
            }
        };
    }

    private void complete(ChatStreamState state, List<ChatCitationData> citations) {
        if (!state.markTerminal()) {
            return;
        }
        try {
            chatPersistenceService.complete(state.chat(), state.content(), citations);
            ChatCompletedEvent event = new ChatCompletedEvent(
                    state.chat().sessionId(), state.chat().assistantMessageId(), citations
            );
            sendCompleted(state, event);
        } catch (RuntimeException error) {
            log.error("保存 AI 回答失败 sessionId={} messageId={}",
                    state.chat().sessionId(), state.chat().assistantMessageId(), error);
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
            log.error("保存 AI 失败状态失败 sessionId={} messageId={}",
                    state.chat().sessionId(), state.chat().assistantMessageId(), persistenceError);
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
            log.debug("客户端在完成事件发送前断开 messageId={}", state.chat().assistantMessageId());
        }
    }

    private void sendFailure(ChatStreamState state) {
        try {
            state.emitter().send(SseEmitter.event().name("error").data(new ChatFailedEvent(RESPONSE_FAILED)));
            state.emitter().complete();
        } catch (IOException | IllegalStateException ignored) {
            // 客户端已经断开时，无需再次尝试写入 SSE。
        }
    }

    private List<ChatCitationData> citations(ChatChunk chunk) {
        List<ChatCitationData> citations = new ArrayList<>();
        for (Citation source : chunk.getCitationsList()) {
            Long noteId = parseNoteId(source.getNoteId());
            String noteTitle = source.getNoteTitle().strip();
            String chunkText = source.getChunkText().strip();
            if (noteId == null || noteTitle.isEmpty() || chunkText.isEmpty()) {
                log.warn("忽略不完整的 AI 引用 noteId={}", source.getNoteId());
                continue;
            }
            citations.add(new ChatCitationData(noteId, noteTitle, chunkText, source.getScore()));
        }
        return List.copyOf(citations);
    }

    private Long parseNoteId(String noteId) {
        try {
            long value = Long.parseLong(noteId);
            return value > 0 ? value : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static final class ChatStreamState {

        private final ChatStart chat;
        private final SseEmitter emitter = new SseEmitter(0L);
        private final AtomicBoolean terminal = new AtomicBoolean();
        private final AtomicReference<Context.CancellableContext> cancellation = new AtomicReference<>();
        private final StringBuilder content = new StringBuilder();

        private ChatStreamState(ChatStart chat) {
            this.chat = chat;
        }

        private ChatStart chat() {
            return chat;
        }

        private SseEmitter emitter() {
            return emitter;
        }

        private boolean isTerminal() {
            return terminal.get();
        }

        private boolean markTerminal() {
            return terminal.compareAndSet(false, true);
        }

        private synchronized void append(String token) {
            content.append(token);
        }

        private synchronized String content() {
            return content.toString();
        }

        private void setCancellation(Context.CancellableContext context) {
            cancellation.set(context);
            if (isTerminal()) {
                context.cancel(null);
            }
        }

        private void cancel() {
            Context.CancellableContext context = cancellation.get();
            if (context != null) {
                context.cancel(null);
            }
        }
    }
}
