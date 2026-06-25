package com.zhousheng.llcb.security;

import com.zhousheng.llcb.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void clear() {
        TestSecurity.clear();
    }

    @Test
    void currentUserIdReturnsAuthenticatedSecurityUser() {
        TestSecurity.loginAs(42L, "qa_user", "learner");

        assertThat(SecurityUtils.currentUserId()).isEqualTo(42L);
        assertThat(SecurityUtils.currentUserIdOrNull()).isEqualTo(42L);
    }

    @Test
    void currentUserIdThrowsWhenUnauthenticated() {
        assertThat(SecurityUtils.currentUserIdOrNull()).isNull();

        assertThatThrownBy(SecurityUtils::currentUserId)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hasRoleMatchesPrefixedAuthorities() {
        TestSecurity.loginAs(42L, "qa_admin", "admin", "auditor");

        assertThat(SecurityUtils.hasRole("admin")).isTrue();
        assertThat(SecurityUtils.hasRole("auditor")).isTrue();
        assertThat(SecurityUtils.hasRole("learner")).isFalse();
    }

    @Test
    void hasRoleReturnsFalseWithoutAuthentication() {
        assertThat(SecurityUtils.hasRole("admin")).isFalse();
    }
}
