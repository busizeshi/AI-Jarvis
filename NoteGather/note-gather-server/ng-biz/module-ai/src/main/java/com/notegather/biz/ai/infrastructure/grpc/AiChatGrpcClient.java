package com.notegather.biz.ai.infrastructure.grpc;

import com.notegather.biz.ai.application.dto.ChatStart;
import com.notegather.common.grpc.config.AiServiceProperties;
import com.notegather.grpc.ai.ChatChunk;
import com.notegather.grpc.ai.ChatRequest;
import com.notegather.grpc.ai.ChatServiceGrpc;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AiChatGrpcClient {

    private final ManagedChannel searchServiceChannel;
    private final AiServiceProperties properties;

    public AiChatGrpcClient(
            @Qualifier("searchServiceChannel") ManagedChannel searchServiceChannel,
            AiServiceProperties properties
    ) {
        this.searchServiceChannel = searchServiceChannel;
        this.properties = properties;
    }

    public Context.CancellableContext stream(ChatStart chat, StreamObserver<ChatChunk> observer) {
        Context.CancellableContext cancellationContext = Context.current().withCancellation();
        cancellationContext.run(() -> ChatServiceGrpc.newStub(searchServiceChannel)
                .withDeadlineAfter(properties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS)
                .chat(ChatRequest.newBuilder()
                        .setUserId(chat.userId().toString())
                        .setQuestion(chat.question())
                        .build(), observer));
        return cancellationContext;
    }
}
