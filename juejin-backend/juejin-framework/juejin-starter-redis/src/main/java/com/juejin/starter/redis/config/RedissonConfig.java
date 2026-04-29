package com.juejin.starter.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置类
 *
 * @author juejin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.redis", name = "host")
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private int database;

    /**
     * 创建 RedissonClient Bean，用于分布式锁、分布式集合等场景
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password.isEmpty() ? null : password)
                .setDatabase(database)
                .setConnectionPoolSize(16)
                .setConnectionMinimumIdleSize(4);

        RedissonClient client = Redisson.create(config);
        log.info("RedissonClient initialized: {}:{}", host, port);
        return client;
    }

}
