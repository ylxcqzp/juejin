package com.juejin.operation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 运营服务启动类
 *
 * @author juejin
 */
@SpringBootApplication(scanBasePackages = {"com.juejin.operation", "com.juejin.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.juejin.common.feign")
public class OperationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperationServiceApplication.class, args);
    }

}
