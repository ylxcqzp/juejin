package com.juejin.starter.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JWT 认证令牌，承载从网关请求头或 Token 解析出的用户信息。
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final String nickname;
    private final int role;

    public JwtAuthenticationToken(Long userId, String nickname, int role) {
        super(buildAuthorities(role));
        this.userId = userId;
        this.nickname = nickname;
        this.role = role;
        setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(int role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (role == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_AUTHOR"));
        }
        if (role == 2) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getRole() {
        return role;
    }
}
