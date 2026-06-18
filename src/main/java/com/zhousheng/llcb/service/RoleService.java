package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleApiMapper roleApiMapper;

    public RoleService(SysRoleMapper roleMapper,
                       SysUserRoleMapper userRoleMapper,
                       SysMenuMapper menuMapper,
                       SysRoleMenuMapper roleMenuMapper,
                       SysRoleApiMapper roleApiMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleApiMapper = roleApiMapper;
    }

    public List<String> roleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .filter(role -> "enabled".equals(role.getStatus()))
                .map(SysRole::getRoleCode)
                .toList();
    }

    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.physicalDeleteByUserId(userId);
        if (roleIds == null) {
            return;
        }
        Long operator = SecurityUtils.currentUserIdOrNull();
        for (Long roleId : roleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setAssignedBy(operator);
            relation.setAssignedAt(LocalDateTime.now());
            userRoleMapper.insert(relation);
        }
    }

    @Transactional
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.physicalDeleteByRoleId(roleId);
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
    }

    @Transactional
    public void assignRoleApis(Long roleId, List<Long> apiIds) {
        roleApiMapper.physicalDeleteByRoleId(roleId);
        if (apiIds == null) {
            return;
        }
        for (Long apiId : apiIds) {
            SysRoleApi relation = new SysRoleApi();
            relation.setRoleId(roleId);
            relation.setApiId(apiId);
            roleApiMapper.insert(relation);
        }
    }

    public List<SysMenu> currentMenus() {
        Long userId = SecurityUtils.currentUserId();
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, "enabled")
                .orderByAsc(SysMenu::getSortNo));
    }
}
