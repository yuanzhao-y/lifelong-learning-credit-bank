package com.zhousheng.llcb;

import com.zhousheng.llcb.security.SecurityUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class TestSecurity {

    private TestSecurity() {
    }

    public static void loginAs(Long userId, String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        SecurityUser user = new SecurityUser(userId, username, "N/A", "enabled", authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, authorities));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
