package com.juejin.gateway.filter;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("#{'${auth.whitelist}'.split(',')}")
    private List<String> whitelist;

    private Key key;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否是白名单路径（支持 METHOD:path 格式限制 HTTP 方法）
        if (isWhitelisted(path, request.getMethodValue())) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "Missing authentication token");
        }

        // 验证Token
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            
            // 将用户ID添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    /**
     * 检查路径是否在白名单中。
     * 支持 METHOD:/path/** 格式限制 HTTP 方法。
     * 如 GET:/api/v1/articles/** 只允许 GET 不鉴权。
     */
    private boolean isWhitelisted(String path, String method) {
        return whitelist.stream()
                .map(String::trim)
                .anyMatch(pattern -> {
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

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
