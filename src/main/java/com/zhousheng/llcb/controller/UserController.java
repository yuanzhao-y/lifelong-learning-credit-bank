package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.dto.UserDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.AuthService;
import com.zhousheng.llcb.service.RoleService;
import com.zhousheng.llcb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final RoleService roleService;
    private final SysUserMapper userMapper;

    public UserController(UserService userService,
                          AuthService authService,
                          RoleService roleService,
                          SysUserMapper userMapper) {
        this.userService = userService;
        this.authService = authService;
        this.roleService = roleService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users/me")
    public ApiResponse<UserDtos.UserProfileResponse> me() {
        return ApiResponse.ok(userService.profile(SecurityUtils.currentUserId()));
    }

    @PutMapping("/users/me")
    public ApiResponse<UserDtos.UserProfileResponse> updateMe(@RequestBody UserDtos.UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/users/me/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/users/me/menus")
    public ApiResponse<?> currentMenus() {
        return ApiResponse.ok(roleService.currentMenus());
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<SysUser>> users(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String realName,
                                            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreatedAt);
        return ApiResponse.ok(userMapper.selectPage(MybatisPlusConfig.page(page, size), wrapper));
    }

    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestBody UserDtos.UserStatusRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!Constants.STATUS_ENABLED.equals(request.status())
                && !Constants.STATUS_DISABLED.equals(request.status())
                && !"frozen".equals(request.status())) {
            throw new BusinessException("用户状态不合法");
        }
        user.setStatus(request.status());
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    @PutMapping("/admin/users/{id}/roles")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody UserDtos.AssignRolesRequest request) {
        roleService.assignUserRoles(id, request.roleIds());
        return ApiResponse.ok();
    }
}
