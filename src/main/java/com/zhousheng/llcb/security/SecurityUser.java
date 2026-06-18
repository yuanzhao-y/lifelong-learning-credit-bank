package com.zhousheng.llcb.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String status;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(Long userId,
                        String username,
                        String password,
                        String status,
                        Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"frozen".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "enabled".equals(status);
    }
}
