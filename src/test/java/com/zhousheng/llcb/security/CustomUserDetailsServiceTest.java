package com.zhousheng.llcb.security;

import com.zhousheng.llcb.TestMybatis;
import com.zhousheng.llcb.entity.SysRole;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.entity.SysUserRole;
import com.zhousheng.llcb.mapper.SysRoleMapper;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @BeforeAll
    static void metadata() {
        TestMybatis.initialize(SysUser.class, SysUserRole.class, SysRole.class);
    }

    @Mock private SysUserMapper userMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysRoleMapper roleMapper;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userMapper, userRoleMapper, roleMapper);
    }

    @Test
    void rejectsUnknownUsername() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void returnsUserWithoutAuthoritiesWhenNoRolesExist() {
        SysUser user = user();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userRoleMapper.selectList(any())).thenReturn(List.of());

        SecurityUser details = (SecurityUser) service.loadUserByUsername("qa_user");

        assertThat(details.getAuthorities()).isEmpty();
        assertThat(details.getPasswordUpdatedAt()).isEqualTo(user.getPasswordUpdatedAt());
    }

    @Test
    void includesOnlyEnabledRoles() {
        when(userMapper.selectOne(any())).thenReturn(user());
        SysUserRole one = relation(1L);
        SysUserRole two = relation(2L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(one, two));
        when(roleMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(
                role("learner", "enabled"), role("admin", "disabled")));

        SecurityUser details = (SecurityUser) service.loadUserByUsername("qa_user");

        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_learner");
    }

    private SysUser user() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("qa_user");
        user.setPasswordHash("hash");
        user.setStatus("enabled");
        user.setPasswordUpdatedAt(LocalDateTime.of(2026, 6, 25, 9, 0));
        return user;
    }

    private SysUserRole relation(Long roleId) {
        SysUserRole relation = new SysUserRole();
        relation.setRoleId(roleId);
        return relation;
    }

    private SysRole role(String code, String status) {
        SysRole role = new SysRole();
        role.setRoleCode(code);
        role.setStatus(status);
        return role;
    }
}
