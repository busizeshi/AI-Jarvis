package com.notegather.common.security.util;

import cn.hutool.core.util.StrUtil;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.model.LoginUser;
import com.notegather.common.security.model.TokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USERNAME = "uname";
    private static final String CLAIM_NICKNAME = "nick";
    private static final String CLAIM_TYPE = "type";
    private static final int JTI_BYTES = 24;

    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAccessToken(LoginUser loginUser) {
        return buildToken(loginUser, TYPE_ACCESS, jwtProperties.getAccessTokenExpireMs());
    }

    public String generateRefreshToken(LoginUser loginUser) {
        return buildToken(loginUser, TYPE_REFRESH, jwtProperties.getRefreshTokenExpireMs());
    }

    public LoginUser parseToken(String token) {
        return parseAccessToken(token).toLoginUser();
    }

    public TokenPayload parseAccessToken(String token) {
        TokenPayload payload = parseTokenPayload(token);
        requireType(payload, TYPE_ACCESS);
        return payload;
    }

    public TokenPayload parseRefreshToken(String token) {
        TokenPayload payload = parseTokenPayload(token);
        requireType(payload, TYPE_REFRESH);
        return payload;
    }

    public TokenPayload parseTokenPayload(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return toPayload(claims);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    public String extractToken(String bearerToken) {
        String prefix = jwtProperties.getTokenPrefix();
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return null;
    }

    private String buildToken(LoginUser loginUser, String type, long expireMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);
        return Jwts.builder()
                .id(generateJti())
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

    private TokenPayload toPayload(Claims claims) {
        TokenPayload payload = TokenPayload.builder()
                .userId(claims.get(CLAIM_USER_ID, String.class))
                .username(claims.get(CLAIM_USERNAME, String.class))
                .nickname(claims.get(CLAIM_NICKNAME, String.class))
                .jti(claims.getId())
                .type(claims.get(CLAIM_TYPE, String.class))
                .issuedAt(toInstant(claims.getIssuedAt()))
                .expiresAt(toInstant(claims.getExpiration()))
                .build();
        validatePayload(payload);
        return payload;
    }

    private void validatePayload(TokenPayload payload) {
        if (StrUtil.isBlank(payload.getUserId()) || StrUtil.isBlank(payload.getJti())
                || StrUtil.isBlank(payload.getType())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    private void requireType(TokenPayload payload, String expectedType) {
        if (!expectedType.equals(payload.getType())) {
            throw new BusinessException(ResultCode.TOKEN_TYPE_INVALID);
        }
    }

    private String generateJti() {
        byte[] bytes = new byte[JTI_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    private SecretKey getSignKey() {
        if (StrUtil.isBlank(jwtProperties.getSecret())) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "JWT 密钥未配置");
        }
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
