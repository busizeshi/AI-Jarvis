package com.notegather.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notegather.common.core.constant.CommonConstants;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.Result;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.model.LoginUser;
import com.notegather.common.security.util.JwtUtils;
import com.notegather.gateway.config.SecurityWhiteListProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * JWT 全局鉴权过滤器
 * <p>
 * 执行顺序（Order = -100，在限流过滤器之后、业务路由之前）：
 * 1. 白名单路径直接放行
 * 2. 提取 Bearer Token
 * 3. 查 Redis 黑名单（已登出 Token）
 * 4. 验证 Token 签名与有效期
 * 5. 透传 X-User-Id / X-Username 到下游
 * 6. 鉴权失败返回 401 JSON
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final SecurityWhiteListProperties whiteListProperties;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /** 在 Sentinel 限流(-1)之后、路由转发之前执行 */
    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单直接放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(jwtProperties.getHeaderName());
        String token = jwtUtils.extractToken(authHeader);
        if (token == null) {
            return writeUnauthorized(exchange, ResultCode.UNAUTHORIZED);
        }

        // 3. 查 Redis 黑名单（响应式，不阻塞事件循环）
        String blacklistKey = CommonConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(blacklistKey)
                .flatMap(inBlacklist -> {
                    if (Boolean.TRUE.equals(inBlacklist)) {
                        log.warn("[JwtAuthFilter] Token 在黑名单中, path={}", path);
                        return writeUnauthorized(exchange, ResultCode.TOKEN_INVALID);
                    }

                    // 4. 验证 Token（JJWT 同步操作，纯 CPU，无 IO 阻塞）
                    LoginUser loginUser;
                    try {
                        loginUser = jwtUtils.parseToken(token);
                    } catch (BusinessException e) {
                        ResultCode resultCode = (e.getCode() == ResultCode.TOKEN_EXPIRED.getCode())
                                ? ResultCode.TOKEN_EXPIRED
                                : ResultCode.TOKEN_INVALID;
                        log.warn("[JwtAuthFilter] Token 验证失败: code={}, path={}", resultCode.getCode(), path);
                        return writeUnauthorized(exchange, resultCode);
                    }

                    // 5. 透传用户信息到下游（覆盖原始请求头，防止客户端伪造）
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header(CommonConstants.HEADER_USER_ID, loginUser.getUserId())
                            .header(CommonConstants.HEADER_USERNAME,
                                    loginUser.getUsername() != null ? loginUser.getUsername() : "")
                            // 移除原始 Authorization，下游服务通过 X-User-Id 识别用户
                            .headers(headers -> headers.remove(CommonConstants.HEADER_AUTHORIZATION))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(ex -> {
                    log.error("[JwtAuthFilter] Redis 黑名单查询异常, path={}", path, ex);
                    // Redis 不可用时降级：继续尝试验证 Token（可按需改为拒绝）
                    return fallbackWithTokenOnly(exchange, chain, token, path);
                });
    }

    /**
     * Redis 不可用时的降级逻辑：仅凭 Token 有效性决策
     */
    private Mono<Void> fallbackWithTokenOnly(ServerWebExchange exchange,
                                             GatewayFilterChain chain,
                                             String token,
                                             String path) {
        try {
            LoginUser loginUser = jwtUtils.parseToken(token);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(CommonConstants.HEADER_USER_ID, loginUser.getUserId())
                    .header(CommonConstants.HEADER_USERNAME,
                            loginUser.getUsername() != null ? loginUser.getUsername() : "")
                    .headers(headers -> headers.remove(CommonConstants.HEADER_AUTHORIZATION))
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (BusinessException e) {
            ResultCode resultCode = (e.getCode() == ResultCode.TOKEN_EXPIRED.getCode())
                    ? ResultCode.TOKEN_EXPIRED
                    : ResultCode.TOKEN_INVALID;
            return writeUnauthorized(exchange, resultCode);
        }
    }

    // ==================== 工具方法 ====================

    private boolean isWhiteListed(String path) {
        return whiteListProperties.getWhiteList().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, ResultCode resultCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.fail(resultCode));
        } catch (JsonProcessingException e) {
            // 序列化失败时用手动拼接兜底
            String fallback = String.format("{\"code\":%d,\"message\":\"%s\"}",
                    resultCode.getCode(), resultCode.getMessage());
            bytes = fallback.getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
