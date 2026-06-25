package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestMybatis;
import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @BeforeAll
    static void metadata() {
        TestMybatis.initialize(SysUserRole.class, SysRole.class, SysRoleMenu.class, SysRoleApi.class, SysMenu.class);
    }

    @Mock private SysRoleMapper roleMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysMenuMapper menuMapper;
    @Mock private SysRoleMenuMapper roleMenuMapper;
    @Mock private SysRoleApiMapper roleApiMapper;

    private RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(roleMapper, userRoleMapper, menuMapper, roleMenuMapper, roleApiMapper);
        TestSecurity.loginAs(1L, "qa_admin", "admin");
    }

    @AfterEach
    void clear() {
        TestSecurity.clear();
    }

    @Test
    void roleCodesReturnsEmptyWhenNoAssignmentExists() {
        when(userRoleMapper.selectList(any())).thenReturn(List.of());

        assertThat(service.roleCodes(7L)).isEmpty();
    }

    @Test
    void roleCodesFiltersDisabledRoles() {
        SysUserRole learnerRelation = relation(1L);
        SysUserRole disabledRelation = relation(2L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(learnerRelation, disabledRelation));
        SysRole learner = role(1L, "learner", "enabled");
        SysRole disabled = role(2L, "auditor", "disabled");
        when(roleMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(learner, disabled));

        assertThat(service.roleCodes(7L)).containsExactly("learner");
    }

    @Test
    void assignUserRolesReplacesRelationsAndRecordsOperator() {
        service.assignUserRoles(7L, List.of(2L, 3L));

        verify(userRoleMapper).physicalDeleteByUserId(7L);
        ArgumentCaptor<SysUserRole> captor = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).extracting(SysUserRole::getRoleId).containsExactly(2L, 3L);
        assertThat(captor.getAllValues()).extracting(SysUserRole::getAssignedBy).containsOnly(1L);
    }

    @Test
    void assigningNullRoleListOnlyClearsExistingRelations() {
        service.assignUserRoles(7L, null);

        verify(userRoleMapper).physicalDeleteByUserId(7L);
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    void currentMenusReturnsDistinctAssignedMenus() {
        when(userRoleMapper.selectList(any())).thenReturn(List.of(relation(2L)));
        SysRoleMenu one = new SysRoleMenu();
        one.setMenuId(10L);
        SysRoleMenu duplicate = new SysRoleMenu();
        duplicate.setMenuId(10L);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(one, duplicate));
        SysMenu menu = new SysMenu();
        menu.setId(10L);
        menu.setMenuCode("dashboard");
        when(menuMapper.selectList(any())).thenReturn(List.of(menu));

        assertThat(service.currentMenus()).extracting(SysMenu::getMenuCode).containsExactly("dashboard");
    }

    private SysUserRole relation(Long roleId) {
        SysUserRole relation = new SysUserRole();
        relation.setRoleId(roleId);
        return relation;
    }

    private SysRole role(Long id, String code, String status) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setStatus(status);
        return role;
    }
}
