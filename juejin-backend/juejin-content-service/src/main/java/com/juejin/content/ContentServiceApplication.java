package com.juejin.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 内容服务启动类
 *
 * @author juejin
 */
@SpringBootApplication(scanBasePackages = {"com.juejin.content", "com.juejin.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.juejin.common.feign")
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }

}
