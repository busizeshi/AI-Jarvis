package com.notegather.admin;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * admin 管理端启动类
 */
@EnableDubbo
@MapperScan("com.notegather.admin.**.mapper")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.notegather")
public class NgAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(NgAdminApplication.class, args);
    }
}
