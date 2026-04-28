package com.juejin.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 社交服务启动类
 *
 * @author juejin
 */
@SpringBootApplication(scanBasePackages = {"com.juejin.social", "com.juejin.common"})
@EnableDiscoveryClient
public class SocialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }

}
