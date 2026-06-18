package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('admin')")
public class AdminSystemController {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysApiPermissionMapper apiPermissionMapper;
    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final RoleService roleService;

    public AdminSystemController(SysRoleMapper roleMapper,
                                 SysMenuMapper menuMapper,
                                 SysApiPermissionMapper apiPermissionMapper,
                                 SysDictMapper dictMapper,
                                 SysDictItemMapper dictItemMapper,
                                 SysOperationLogMapper operationLogMapper,
                                 RoleService roleService) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.apiPermissionMapper = apiPermissionMapper;
        this.dictMapper = dictMapper;
        this.dictItemMapper = dictItemMapper;
        this.operationLogMapper = operationLogMapper;
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<SysRole>> roles() {
        return ApiResponse.ok(roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortNo)));
    }

    @PostMapping("/roles")
    public ApiResponse<SysRole> createRole(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return ApiResponse.ok(role);
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return ApiResponse.ok();
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PutMapping("/roles/{id}/menus")
    public ApiResponse<Void> assignRoleMenus(@PathVariable Long id, @RequestBody IdsRequest request) {
        roleService.assignRoleMenus(id, request.ids());
        return ApiResponse.ok();
    }

    @PutMapping("/roles/{id}/apis")
    public ApiResponse<Void> assignRoleApis(@PathVariable Long id, @RequestBody IdsRequest request) {
        roleService.assignRoleApis(id, request.ids());
        return ApiResponse.ok();
    }

    @GetMapping("/menus")
    public ApiResponse<List<SysMenu>> menus() {
        return ApiResponse.ok(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortNo)));
    }

    @PostMapping("/menus")
    public ApiResponse<SysMenu> createMenu(@RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return ApiResponse.ok(menu);
    }

    @PutMapping("/menus/{id}")
    public ApiResponse<Void> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        menuMapper.updateById(menu);
        return ApiResponse.ok();
    }

    @DeleteMapping("/menus/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        menuMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/api-permissions")
    public ApiResponse<List<SysApiPermission>> apis() {
        return ApiResponse.ok(apiPermissionMapper.selectList(new LambdaQueryWrapper<SysApiPermission>().orderByAsc(SysApiPermission::getModule)));
    }

    @PostMapping("/api-permissions")
    public ApiResponse<SysApiPermission> createApi(@RequestBody SysApiPermission api) {
        apiPermissionMapper.insert(api);
        return ApiResponse.ok(api);
    }

    @PutMapping("/api-permissions/{id}")
    public ApiResponse<Void> updateApi(@PathVariable Long id, @RequestBody SysApiPermission api) {
        api.setId(id);
        apiPermissionMapper.updateById(api);
        return ApiResponse.ok();
    }

    @DeleteMapping("/api-permissions/{id}")
    public ApiResponse<Void> deleteApi(@PathVariable Long id) {
        apiPermissionMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dicts")
    public ApiResponse<List<SysDict>> dicts() {
        return ApiResponse.ok(dictMapper.selectList(new LambdaQueryWrapper<SysDict>().orderByAsc(SysDict::getSortNo)));
    }

    @PostMapping("/dicts")
    public ApiResponse<SysDict> createDict(@RequestBody SysDict dict) {
        dictMapper.insert(dict);
        return ApiResponse.ok(dict);
    }

    @PutMapping("/dicts/{id}")
    public ApiResponse<Void> updateDict(@PathVariable Long id, @RequestBody SysDict dict) {
        dict.setId(id);
        dictMapper.updateById(dict);
        return ApiResponse.ok();
    }

    @DeleteMapping("/dicts/{id}")
    public ApiResponse<Void> deleteDict(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dicts/{dictId}/items")
    public ApiResponse<List<SysDictItem>> dictItems(@PathVariable Long dictId) {
        return ApiResponse.ok(dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dictId)
                .orderByAsc(SysDictItem::getSortNo)));
    }

    @PostMapping("/dicts/{dictId}/items")
    public ApiResponse<SysDictItem> createDictItem(@PathVariable Long dictId, @RequestBody SysDictItem item) {
        item.setDictId(dictId);
        dictItemMapper.insert(item);
        return ApiResponse.ok(item);
    }

    @PutMapping("/dict-items/{id}")
    public ApiResponse<Void> updateDictItem(@PathVariable Long id, @RequestBody SysDictItem item) {
        item.setId(id);
        dictItemMapper.updateById(item);
        return ApiResponse.ok();
    }

    @DeleteMapping("/dict-items/{id}")
    public ApiResponse<Void> deleteDictItem(@PathVariable Long id) {
        dictItemMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/operation-logs")
    public ApiResponse<Page<SysOperationLog>> logs(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Long operatorId,
                                                   @RequestParam(required = false) String operationType) {
        return ApiResponse.ok(operationLogMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(operatorId != null, SysOperationLog::getOperatorId, operatorId)
                        .eq(StringUtils.hasText(operationType), SysOperationLog::getOperationType, operationType)
                        .orderByDesc(SysOperationLog::getOperatedAt)));
    }

    public record IdsRequest(List<Long> ids) {
    }
}
