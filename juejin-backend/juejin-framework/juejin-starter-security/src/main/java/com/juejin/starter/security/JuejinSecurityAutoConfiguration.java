package com.juejin.starter.security;

import com.juejin.starter.security.utils.JwtUtils;
import com.juejin.starter.security.utils.SecurityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Security自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@Import({
        JwtUtils.class,
        SecurityUtils.class
})
public class JuejinSecurityAutoConfiguration {

}
