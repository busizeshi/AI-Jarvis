package com.notegather.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ng.security.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpireMs = 2 * 60 * 60 * 1000L;
    private long refreshTokenExpireMs = 30L * 24 * 60 * 60 * 1000L;
    private String headerName = "Authorization";
    private String tokenPrefix = "Bearer ";
}
