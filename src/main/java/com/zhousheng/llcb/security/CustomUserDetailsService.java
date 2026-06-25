package com.zhousheng.llcb.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.entity.SysRole;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.entity.SysUserRole;
import com.zhousheng.llcb.mapper.SysRoleMapper;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.mapper.SysUserRoleMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    public CustomUserDetailsService(SysUserMapper userMapper,
                                    SysUserRoleMapper userRoleMapper,
                                    SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        List<SimpleGrantedAuthority> authorities = roleIds.isEmpty()
                ? List.of()
                : roleMapper.selectBatchIds(roleIds).stream()
                .filter(role -> "enabled".equals(role.getStatus()))
                .map(SysRole::getRoleCode)
                .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                .toList();
        return new SecurityUser(user.getId(), user.getUsername(), user.getPasswordHash(), user.getStatus(),
                user.getPasswordUpdatedAt(), authorities);
    }
}
