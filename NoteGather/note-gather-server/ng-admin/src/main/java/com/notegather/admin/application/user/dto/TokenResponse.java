package com.notegather.admin.application.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenResponse {

    String accessToken;
    String refreshToken;
    String tokenType;
    long accessTokenExpiresIn;
    long refreshTokenExpiresIn;
    UserResponse user;
}
