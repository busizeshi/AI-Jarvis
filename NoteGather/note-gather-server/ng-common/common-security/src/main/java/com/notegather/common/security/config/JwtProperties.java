package com.notegather.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 * 对应配置项前缀：ng.security.jwt
 */
@Data
@Component
@ConfigurationProperties(prefix = "ng.security.jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥（Base64编码，建议64位以上），生产环境务必通过配置中心注入
     */
    private String secret = "NoteGatherDefaultSecretKeyForDevelopmentOnly2024";

    /**
     * Access Token 有效期（毫秒），默认 2 小时
     */
    private long accessTokenExpireMs = 2 * 60 * 60 * 1000L;

    /**
     * Refresh Token 有效期（毫秒），默认 30 天
     */
    private long refreshTokenExpireMs = 30L * 24 * 60 * 60 * 1000L;

    /**
     * Token 请求头名称
     */
    private String headerName = "Authorization";

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";
}
