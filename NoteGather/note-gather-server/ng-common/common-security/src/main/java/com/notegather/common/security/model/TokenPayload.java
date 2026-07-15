package com.notegather.common.security.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class TokenPayload {

    String userId;
    String username;
    String nickname;
    String jti;
    String type;
    Instant issuedAt;
    Instant expiresAt;

    public LoginUser toLoginUser() {
        return LoginUser.builder()
                .userId(userId)
                .username(username)
                .nickname(nickname)
                .accessTokenJti(jti)
                .accessTokenExpiresAt(expiresAt == null ? null : expiresAt.toEpochMilli())
                .build();
    }

    public long remainingMillis() {
        if (expiresAt == null) {
            return 0L;
        }
        return Math.max(0L, expiresAt.toEpochMilli() - System.currentTimeMillis());
    }
}
