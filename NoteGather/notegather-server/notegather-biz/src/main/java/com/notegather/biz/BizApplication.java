package com.notegather.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * NoteGather 业务服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BizApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
