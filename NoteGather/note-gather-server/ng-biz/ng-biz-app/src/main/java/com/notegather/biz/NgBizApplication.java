package com.notegather.biz;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * biz 业务服务启动类
 */
@EnableDubbo
@MapperScan("com.notegather.biz.**.mapper")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.notegather")
public class NgBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(NgBizApplication.class, args);
    }
}
