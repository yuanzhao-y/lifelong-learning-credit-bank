package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.CsvSecurity;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.service.RoleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('admin')")
public class AdminSystemController {

    private static final int MAX_LOG_EXPORT_ROWS = 10_000;

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
                                                   @RequestParam(required = false) String operationType,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   LocalDateTime startTime,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   LocalDateTime endTime) {
        return ApiResponse.ok(operationLogMapper.selectPage(MybatisPlusConfig.page(page, size),
                operationLogQuery(operatorId, operationType, startTime, endTime)));
    }

    @GetMapping("/operation-logs/export")
    public ResponseEntity<byte[]> exportLogs(@RequestParam(required = false) Long operatorId,
                                             @RequestParam(required = false) String operationType,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime startTime,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime endTime) {
        List<SysOperationLog> logs = operationLogMapper.selectList(
                operationLogQuery(operatorId, operationType, startTime, endTime)
                        .last("LIMIT " + (MAX_LOG_EXPORT_ROWS + 1)));
        if (logs.size() > MAX_LOG_EXPORT_ROWS) {
            throw new BusinessException("导出结果超过 10000 行，请缩小筛选范围");
        }
        StringBuilder builder = new StringBuilder("\uFEFFoperator_id,operator_name,module,operation_type,operation_content,ip_address,operated_at\n");
        for (SysOperationLog log : logs) {
            builder.append(CsvSecurity.cell(log.getOperatorId())).append(',')
                    .append(CsvSecurity.cell(log.getOperatorName())).append(',')
                    .append(CsvSecurity.cell(log.getModule())).append(',')
                    .append(CsvSecurity.cell(log.getOperationType())).append(',')
                    .append(CsvSecurity.cell(log.getOperationContent())).append(',')
                    .append(CsvSecurity.cell(log.getIpAddress())).append(',')
                    .append(CsvSecurity.cell(log.getOperatedAt())).append('\n');
        }
        String filename = "operation-logs-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    public record IdsRequest(List<Long> ids) {
    }

    private LambdaQueryWrapper<SysOperationLog> operationLogQuery(Long operatorId,
                                                                  String operationType,
                                                                  LocalDateTime startTime,
                                                                  LocalDateTime endTime) {
        return new LambdaQueryWrapper<SysOperationLog>()
                .eq(operatorId != null, SysOperationLog::getOperatorId, operatorId)
                .eq(StringUtils.hasText(operationType), SysOperationLog::getOperationType, operationType)
                .ge(startTime != null, SysOperationLog::getOperatedAt, startTime)
                .le(endTime != null, SysOperationLog::getOperatedAt, endTime)
                .orderByDesc(SysOperationLog::getOperatedAt);
    }

}
