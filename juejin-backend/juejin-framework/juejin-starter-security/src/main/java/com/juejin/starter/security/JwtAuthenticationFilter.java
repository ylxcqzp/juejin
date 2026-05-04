package com.juejin.starter.security;

import com.juejin.starter.security.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 * <p>
 * 认证优先级：
 * 1. 从 X-User-Id 请求头获取（网关已解析 Token 并注入）
 * 2. 从 Authorization Bearer Token 自行解析（直连或 Feign 调用）
 * 3. 都没有 → 根据路径决定放行（公开路径）或拒绝（需认证路径）
 * <p>
 * 配置示例（application.yml）：
 * <pre>
 * security:
 *   public-paths: GET:/api/v1/articles/**,GET:/api/v1/users/**
 * </pre>
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final List<String> publicPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(List<String> publicPaths) {
        this.publicPaths = publicPaths != null ? publicPaths : List.of();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. 尝试从 X-User-Id 请求头获取（网关注入）
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                setAuthentication(userId, request);
                filterChain.doFilter(request, response);
                return;
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userIdHeader);
            }
        }

        // 2. 尝试从 Authorization Bearer Token 自行解析
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            try {
                if (JwtUtils.validateToken(token)) {
                    Long userId = JwtUtils.getUserIdFromToken(token);
                    if (userId != null) {
                        setAuthentication(userId, request);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
            }
        }

        // 3. 检查是否是公开路径
        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 无认证信息且非公开路径 → 401
        log.warn("Authentication required for {} {}", method, path);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\":401,\"message\":\"未认证，请先登录\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
    }

    private void setAuthentication(Long userId, HttpServletRequest request) {
        // 尝试获取用户信息（通过 Feign 或请求属性缓存）
        String nickname = request.getHeader("X-User-Nickname");
        String roleHeader = request.getHeader("X-User-Role");
        int role = 0;
        if (StringUtils.hasText(roleHeader)) {
            try {
                role = Integer.parseInt(roleHeader);
            } catch (NumberFormatException ignored) {
            }
        }

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, nickname, role);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path, String method) {
        return publicPaths.stream().anyMatch(pattern -> {
            int colonIdx = pattern.indexOf(':');
            if (colonIdx > 0 && colonIdx < 8) {
                String requireMethod = pattern.substring(0, colonIdx);
                String pathPattern = pattern.substring(colonIdx + 1);
                return method.equalsIgnoreCase(requireMethod)
                        && pathMatcher.match(pathPattern, path);
            }
            return pathMatcher.match(pattern, path);
        });
    }
}
