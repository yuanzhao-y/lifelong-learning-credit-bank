package com.zhousheng.llcb.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidToken() throws Exception {
        JwtService jwtService = jwtService();
        UserDetailsService users = mock(UserDetailsService.class);
        when(users.loadUserByUsername("qa_user")).thenReturn(user(LocalDateTime.now().minusMinutes(1)));
        String token = jwtService.createToken(7L, "qa_user", Map.of());
        FilterChain chain = mock(FilterChain.class);

        filter(jwtService, users).doFilter(request(token), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsTokenIssuedBeforePasswordChange() throws Exception {
        JwtService jwtService = jwtService();
        UserDetailsService users = mock(UserDetailsService.class);
        when(users.loadUserByUsername("qa_user")).thenReturn(user(LocalDateTime.now().plusMinutes(1)));
        String token = jwtService.createToken(7L, "qa_user", Map.of());

        filter(jwtService, users).doFilter(request(token), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoresTamperedToken() throws Exception {
        JwtService jwtService = jwtService();
        String token = jwtService.createToken(7L, "qa_user", Map.of()) + "bad";

        filter(jwtService, mock(UserDetailsService.class))
                .doFilter(request(token), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void acceptsMatchingPasswordVersionClaim() throws Exception {
        JwtService jwtService = jwtService();
        LocalDateTime passwordUpdatedAt = LocalDateTime.now();
        UserDetailsService users = mock(UserDetailsService.class);
        when(users.loadUserByUsername("qa_user")).thenReturn(user(passwordUpdatedAt));
        long version = passwordUpdatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String token = jwtService.createToken(7L, "qa_user", Map.of("pwdAt", version));

        filter(jwtService, users).doFilter(request(token), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void skipsRequestWithoutBearerToken() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        filter(jwtService(), mock(UserDetailsService.class))
                .doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void keepsExistingAuthenticationWhenPresent() throws Exception {
        JwtService jwtService = jwtService();
        UserDetailsService users = mock(UserDetailsService.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, List.of()));
        String token = jwtService.createToken(7L, "qa_user", Map.of());

        filter(jwtService, users).doFilter(request(token), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("existing");
    }

    @Test
    void rejectsMismatchedPasswordVersionClaim() throws Exception {
        JwtService jwtService = jwtService();
        LocalDateTime passwordUpdatedAt = LocalDateTime.now();
        UserDetailsService users = mock(UserDetailsService.class);
        when(users.loadUserByUsername("qa_user")).thenReturn(user(passwordUpdatedAt));
        String token = jwtService.createToken(7L, "qa_user", Map.of("pwdAt", 1L));

        filter(jwtService, users).doFilter(request(token), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private JwtAuthenticationFilter filter(JwtService jwtService, UserDetailsService users) {
        return new JwtAuthenticationFilter(jwtService, users);
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private JwtService jwtService() {
        return new JwtService(new JwtProperties("llcb-test",
                "test-secret-change-me-please-32bytes-min", 60));
    }

    private SecurityUser user(LocalDateTime passwordUpdatedAt) {
        return new SecurityUser(7L, "qa_user", "hash", "enabled", passwordUpdatedAt,
                List.of(new SimpleGrantedAuthority("ROLE_learner")));
    }
}
