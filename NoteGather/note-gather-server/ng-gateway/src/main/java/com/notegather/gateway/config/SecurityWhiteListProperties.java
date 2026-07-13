package com.notegather.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴权白名单配置
 * 对应配置项：ng.security.white-list
 */
@Data
@Component
@ConfigurationProperties(prefix = "ng.security")
public class SecurityWhiteListProperties {

    /**
     * 不校验 Token 的路径列表（支持 Ant 风格通配符）
     */
    private List<String> whiteList = new ArrayList<>();
}
