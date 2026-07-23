package com.notegather.biz.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 */
@Configuration
@MapperScan("com.notegather.biz.infrastructure.persistence.mapper")
public class MyBatisPlusConfig {
}
