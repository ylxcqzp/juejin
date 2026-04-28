package com.juejin.starter.security;

import com.juejin.starter.security.utils.JwtUtils;
import com.juejin.starter.security.utils.SecurityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Security自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnProperty(prefix = "jwt", name = "secret")
@Import({
        JwtUtils.class,
        SecurityUtils.class
})
public class JuejinSecurityAutoConfiguration {

}
