package com.zhousheng.llcb.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.time.Instant;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                String username = claims.getSubject();
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails details = userDetailsService.loadUserByUsername(username);
                    if (details instanceof SecurityUser securityUser && securityUser.getPasswordUpdatedAt() != null) {
                        Instant passwordUpdatedAt = securityUser.getPasswordUpdatedAt()
                                .atZone(ZoneId.systemDefault()).toInstant();
                        Number tokenPasswordVersion = claims.get("pwdAt", Number.class);
                        boolean stalePasswordVersion = tokenPasswordVersion != null
                                && tokenPasswordVersion.longValue() != passwordUpdatedAt.toEpochMilli();
                        boolean staleLegacyToken = tokenPasswordVersion == null
                                && claims.getIssuedAt() != null
                                && claims.getIssuedAt().toInstant().plusSeconds(1).isBefore(passwordUpdatedAt);
                        if (stalePasswordVersion || staleLegacyToken) {
                            throw new IllegalArgumentException("Token was issued before the latest password change");
                        }
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            details, null, details.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
