package com.juejin.starter.redis;

import com.juejin.starter.redis.config.RedisConfig;
import com.juejin.starter.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@Import({
        RedisConfig.class,
        RedisUtils.class
})
public class JuejinRedisAutoConfiguration {

}
