package com.notegather.common.security.config;

import com.notegather.common.security.util.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * common-security 自动配置入口
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    public JwtUtils jwtUtils(JwtProperties jwtProperties) {
        return new JwtUtils(jwtProperties);
    }
}
