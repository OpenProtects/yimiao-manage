package com.yimiao.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yimiao")
@MapperScan("com.yimiao.admin.mapper")
@ComponentScan(basePackages = "com.yimiao")
@EnableScheduling
public class YimiaoAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(YimiaoAdminApplication.class, args);
    }
}
