package com.notegather.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.notegather.common.mybatis.handler.MybatisMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 全局配置
 */
@AutoConfiguration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 插件链
     * - PaginationInnerInterceptor：分页（MySQL）
     * - OptimisticLockerInnerInterceptor：乐观锁（@Version 注解支持）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件（MySQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MybatisMetaObjectHandler mybatisMetaObjectHandler() {
        return new MybatisMetaObjectHandler();
    }
}
