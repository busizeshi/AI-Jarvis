package com.notegather.common.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.model.LoginUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USERNAME = "uname";
    private static final String CLAIM_NICKNAME = "nick";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    // ==================== 生成 Token ====================

    /** 生成 Access Token */
    public String generateAccessToken(LoginUser loginUser) {
        return buildToken(loginUser, TYPE_ACCESS, jwtProperties.getAccessTokenExpireMs());
    }

    /** 生成 Refresh Token */
    public String generateRefreshToken(LoginUser loginUser) {
        return buildToken(loginUser, TYPE_REFRESH, jwtProperties.getRefreshTokenExpireMs());
    }

    private String buildToken(LoginUser loginUser, String type, long expireMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);
        return Jwts.builder()
                .subject(loginUser.getUserId())
                .claim(CLAIM_USER_ID, loginUser.getUserId())
                .claim(CLAIM_USERNAME, loginUser.getUsername())
                .claim(CLAIM_NICKNAME, loginUser.getNickname())
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignKey())
                .compact();
    }

    // ==================== 解析 Token ====================

    /**
     * 解析 Token，返回 LoginUser。
     * Token 无效或过期时抛出 BusinessException
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return LoginUser.builder()
                    .userId(claims.get(CLAIM_USER_ID, String.class))
                    .username(claims.get(CLAIM_USERNAME, String.class))
                    .nickname(claims.get(CLAIM_NICKNAME, String.class))
                    .build();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 从请求头值（"Bearer xxx"）中提取裸 Token
     */
    public String extractToken(String bearerToken) {
        String prefix = jwtProperties.getTokenPrefix();
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * 判断 Token 是否即将过期（剩余时间 < 30分钟时触发无感刷新提示）
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            long remainMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            return remainMs < 30 * 60 * 1000L;
        } catch (Exception e) {
            return true;
        }
    }

    /** 获取 Token 类型（access / refresh） */
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get(CLAIM_TYPE, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(getTokenType(token));
    }

    // ==================== 工具 ====================

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
