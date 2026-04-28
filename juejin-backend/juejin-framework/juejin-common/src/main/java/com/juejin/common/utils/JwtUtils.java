package com.juejin.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 *
 * @author juejin
 */
@Slf4j
public class JwtUtils {

    private static final String SECRET = "juejin-secret-key-for-jwt-signing-2024";
    private static final long EXPIRATION = 1800; // 30分钟
    private static final long REFRESH_EXPIRATION = 604800; // 7天
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成访问Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新Token
     *
     * @param userId 用户ID
     * @return 刷新Token
     */
    public static String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_EXPIRATION * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取Token过期时间
     *
     * @return 过期时间（秒）
     */
    public static long getExpiration() {
        return EXPIRATION;
    }
}
