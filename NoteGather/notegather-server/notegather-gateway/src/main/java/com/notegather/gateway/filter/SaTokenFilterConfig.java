package com.notegather.gateway.filter;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 路由拦截器配置
 */
@Configuration
public class SaTokenFilterConfig {
    
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
            .addInclude("/**")
            .addExclude("/api/auth/login")
            .addExclude("/api/auth/register")
            .addExclude("/actuator/**")
            .setAuth(obj -> {
                SaRouter.match("/**", r -> StpUtil.checkLogin());
            });
    }
}
