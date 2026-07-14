package com.notegather.common.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * gRPC 客户端配置属性（连接 Python AI 服务）
 * 配置项前缀：ng.grpc.ai
 */
@Data
@ConfigurationProperties(prefix = "ng.grpc.ai")
public class AiServiceProperties {

    /** Python parse_service gRPC 地址 */
    private String parseHost = "localhost";
    private int parsePort = 50051;

    /** Python search_service / chat_service gRPC 地址 */
    private String searchHost = "localhost";
    private int searchPort = 50052;

    /** 最大消息大小（字节），默认 16MB */
    private int maxMessageSize = 16 * 1024 * 1024;

    /** 连接超时（毫秒） */
    private long connectTimeoutMs = 5000L;

    /** 请求超时（毫秒） */
    private long requestTimeoutMs = 30000L;
}
