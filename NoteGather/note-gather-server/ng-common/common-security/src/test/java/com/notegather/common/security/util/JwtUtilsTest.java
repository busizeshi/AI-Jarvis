package com.notegather.common.security.util;

import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.model.LoginUser;
import com.notegather.common.security.model.TokenPayload;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils(jwtProperties());

    @Test
    void shouldParseAccessTokenWithJtiAndType() {
        String token = jwtUtils.generateAccessToken(loginUser());

        TokenPayload payload = jwtUtils.parseAccessToken(token);

        assertThat(payload.getUserId()).isEqualTo("1001");
        assertThat(payload.getType()).isEqualTo(JwtUtils.TYPE_ACCESS);
        assertThat(payload.getJti()).isNotBlank();
    }

    @Test
    void shouldRejectRefreshTokenAsAccessToken() {
        String token = jwtUtils.generateRefreshToken(loginUser());

        assertThatThrownBy(() -> jwtUtils.parseAccessToken(token))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ResultCode.TOKEN_TYPE_INVALID.getCode()));
    }

    @Test
    void shouldParseRefreshTokenWithJtiAndType() {
        String token = jwtUtils.generateRefreshToken(loginUser());

        TokenPayload payload = jwtUtils.parseRefreshToken(token);

        assertThat(payload.getUserId()).isEqualTo("1001");
        assertThat(payload.getType()).isEqualTo(JwtUtils.TYPE_REFRESH);
        assertThat(payload.getJti()).isNotBlank();
    }

    private JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef");
        return properties;
    }

    private LoginUser loginUser() {
        return LoginUser.builder()
                .userId("1001")
                .username("alice")
                .nickname("Alice")
                .build();
    }
}
