package com.juejin.starter.security.config;

import com.juejin.starter.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security 配置。
 * <p>
 * 认证流程：
 * 1. JwtAuthenticationFilter 从 X-User-Id 请求头（网关注入）或 Bearer Token 解析用户
 * 2. 解析成功 → 注入 SecurityContext；失败且非公开路径 → 401
 * <p>
 * 各服务通过 {@code security.public-paths} 配置自己的公开端点。
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("#{'${security.public-paths:}'.split(',')}")
    private List<String> publicPaths;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        List<String> paths = publicPaths.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return new JwtAuthenticationFilter(paths);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(jwtFilter, AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
