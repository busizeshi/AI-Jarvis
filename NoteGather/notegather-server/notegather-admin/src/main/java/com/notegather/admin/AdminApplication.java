package com.notegather.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * NoteGather 管理端服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
