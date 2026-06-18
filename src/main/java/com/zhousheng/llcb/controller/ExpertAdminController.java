package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.ExpertProfile;
import com.zhousheng.llcb.mapper.ExpertProfileMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/experts")
@PreAuthorize("hasRole('admin')")
public class ExpertAdminController {

    private final ExpertProfileMapper expertMapper;

    public ExpertAdminController(ExpertProfileMapper expertMapper) {
        this.expertMapper = expertMapper;
    }

    @GetMapping
    public ApiResponse<Page<ExpertProfile>> list(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) String direction,
                                                 @RequestParam(required = false) String status) {
        return ApiResponse.ok(expertMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<ExpertProfile>()
                        .like(StringUtils.hasText(direction), ExpertProfile::getProfessionalDirection, direction)
                        .eq(StringUtils.hasText(status), ExpertProfile::getStatus, status)
                        .orderByDesc(ExpertProfile::getCreatedAt)));
    }

    @PostMapping
    public ApiResponse<ExpertProfile> create(@RequestBody ExpertProfile expert) {
        expertMapper.insert(expert);
        return ApiResponse.ok(expert);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ExpertProfile expert) {
        expert.setId(id);
        expertMapper.updateById(expert);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        expertMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @RequestParam String status) {
        ExpertProfile expert = expertMapper.selectById(id);
        expert.setStatus(status);
        expertMapper.updateById(expert);
        return ApiResponse.ok();
    }
}
