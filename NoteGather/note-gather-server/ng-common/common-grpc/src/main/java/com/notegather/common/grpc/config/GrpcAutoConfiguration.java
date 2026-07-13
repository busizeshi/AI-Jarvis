package com.notegather.common.grpc.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * gRPC 客户端自动配置
 * 通过 Spring Bean 管理 ManagedChannel 生命周期
 */
@AutoConfiguration
@EnableConfigurationProperties(AiServiceProperties.class)
public class GrpcAutoConfiguration {

    /**
     * parse_service channel
     */
    @Bean(name = "parseServiceChannel", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "ng.grpc.ai", name = "parse-host")
    public ManagedChannel parseServiceChannel(AiServiceProperties props) {
        return ManagedChannelBuilder
                .forAddress(props.getParseHost(), props.getParsePort())
                .usePlaintext()  // 开发环境不启用TLS；生产环境改为 useTransportSecurity()
                .maxInboundMessageSize(props.getMaxMessageSize())
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * search_service / chat_service channel
     */
    @Bean(name = "searchServiceChannel", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "ng.grpc.ai", name = "search-host")
    public ManagedChannel searchServiceChannel(AiServiceProperties props) {
        return ManagedChannelBuilder
                .forAddress(props.getSearchHost(), props.getSearchPort())
                .usePlaintext()
                .maxInboundMessageSize(props.getMaxMessageSize())
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .build();
    }
}
