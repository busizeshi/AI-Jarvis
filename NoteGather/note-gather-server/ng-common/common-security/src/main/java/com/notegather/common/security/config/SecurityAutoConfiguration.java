package com.notegather.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notegather.common.security.util.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfiguration
@EnableConfigurationProperties({JwtProperties.class, UserContextProperties.class})
public class SecurityAutoConfiguration {

    @Bean
    public JwtUtils jwtUtils(JwtProperties jwtProperties) {
        return new JwtUtils(jwtProperties);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    static class ServletSecurityConfiguration {

        @Bean
        public FilterRegistrationBean<com.notegather.common.security.filter.UserContextFilter> userContextFilter(
                UserContextProperties properties,
                ObjectMapper objectMapper) {
            FilterRegistrationBean<com.notegather.common.security.filter.UserContextFilter> registration =
                    new FilterRegistrationBean<>();
            registration.setFilter(new com.notegather.common.security.filter.UserContextFilter(properties, objectMapper));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }
}
