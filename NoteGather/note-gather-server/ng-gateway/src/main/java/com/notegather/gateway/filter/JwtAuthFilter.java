package com.notegather.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notegather.common.core.constant.CommonConstants;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.Result;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.redis.session.AuthSessionStore;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.model.TokenPayload;
import com.notegather.common.security.util.JwtUtils;
import com.notegather.gateway.config.SecurityWhiteListProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final SecurityWhiteListProperties whiteListProperties;
    private final AuthSessionStore authSessionStore;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhiteListed(path)) {
            return chain.filter(removeInternalHeaders(exchange));
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(jwtProperties.getHeaderName());
        String token = jwtUtils.extractToken(authHeader);
        if (token == null && path.startsWith("/ws/collab/")) {
            token = exchange.getRequest().getQueryParams().getFirst("access_token");
        }
        if (token == null) {
            return writeUnauthorized(exchange, ResultCode.UNAUTHORIZED);
        }

        TokenPayload payload;
        try {
            payload = jwtUtils.parseAccessToken(token);
        } catch (BusinessException e) {
            return writeUnauthorized(exchange, resolveTokenError(e));
        }

        return Mono.fromCallable(() -> isActiveSession(payload))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(active -> !active
                        ? writeUnauthorized(exchange, ResultCode.TOKEN_INVALID)
                        : chain.filter(withTrustedUserHeaders(exchange, payload)))
                .onErrorResume(ex -> {
                    log.error("[JwtAuthFilter] Redisson session check failed path={}", path, ex);
                    return writeUnavailable(exchange);
                });
    }

    private boolean isActiveSession(TokenPayload payload) {
        return !authSessionStore.isBlacklisted(payload.getJti())
                && authSessionStore.isAccessSessionActive(payload.getUserId(), payload.getJti());
    }

    private ServerWebExchange removeInternalHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(CommonConstants.HEADER_USER_ID);
                    headers.remove(CommonConstants.HEADER_USERNAME);
                    headers.remove(CommonConstants.HEADER_TOKEN_JTI);
                    headers.remove(CommonConstants.HEADER_TOKEN_EXPIRES_AT);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withTrustedUserHeaders(ServerWebExchange exchange, TokenPayload payload) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(CommonConstants.HEADER_AUTHORIZATION);
                    headers.remove(CommonConstants.HEADER_USER_ID);
                    headers.remove(CommonConstants.HEADER_USERNAME);
                    headers.remove(CommonConstants.HEADER_TOKEN_JTI);
                    headers.remove(CommonConstants.HEADER_TOKEN_EXPIRES_AT);
                })
                .header(CommonConstants.HEADER_USER_ID, payload.getUserId())
                .header(CommonConstants.HEADER_USERNAME, payload.getUsername() == null ? "" : payload.getUsername())
                .header(CommonConstants.HEADER_TOKEN_JTI, payload.getJti())
                .header(CommonConstants.HEADER_TOKEN_EXPIRES_AT, String.valueOf(payload.getExpiresAt().toEpochMilli()))
                .build();
        return exchange.mutate().request(request).build();
    }

    private ResultCode resolveTokenError(BusinessException e) {
        if (e.getCode() == ResultCode.TOKEN_EXPIRED.getCode()) {
            return ResultCode.TOKEN_EXPIRED;
        }
        if (e.getCode() == ResultCode.TOKEN_TYPE_INVALID.getCode()) {
            return ResultCode.TOKEN_TYPE_INVALID;
        }
        return ResultCode.TOKEN_INVALID;
    }

    private boolean isWhiteListed(String path) {
        return whiteListProperties.getWhiteList().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, ResultCode resultCode) {
        return writeJson(exchange, HttpStatus.UNAUTHORIZED, Result.fail(resultCode));
    }

    private Mono<Void> writeUnavailable(ServerWebExchange exchange) {
        return writeJson(exchange, HttpStatus.SERVICE_UNAVAILABLE, Result.fail(ResultCode.SERVICE_UNAVAILABLE));
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, Result<Void> result) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            String fallback = String.format("{\"code\":%d,\"message\":\"%s\"}",
                    result.getCode(), result.getMessage());
            bytes = fallback.getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
