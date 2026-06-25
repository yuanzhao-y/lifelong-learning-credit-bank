package com.zhousheng.llcb.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void createsAndParsesToken() {
        JwtService service = new JwtService(properties());

        String token = service.createToken(7L, "qa_user", Map.of("roles", List.of("learner")));
        Claims claims = service.parse(token);

        assertThat(claims.getSubject()).isEqualTo("qa_user");
        assertThat(claims.getId()).isEqualTo("7");
        assertThat(claims.getIssuer()).isEqualTo("llcb-test");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertThat(roles).containsExactly("learner");
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(properties());
        String token = service.createToken(7L, "qa_user", Map.of());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> service.parse(tampered))
                .isInstanceOf(RuntimeException.class);
    }

    private JwtProperties properties() {
        return new JwtProperties("llcb-test", "test-secret-change-me-please-32bytes-min", 60);
    }
}
