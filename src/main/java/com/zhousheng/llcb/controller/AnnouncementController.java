package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.HtmlSecurity;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.SysAnnouncement;
import com.zhousheng.llcb.mapper.SysAnnouncementMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class AnnouncementController {

    private final SysAnnouncementMapper announcementMapper;

    public AnnouncementController(SysAnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @GetMapping("/public/announcements")
    public ApiResponse<Page<SysAnnouncement>> publicList(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size,
                                                         @RequestParam(required = false) String keyword) {
        Page<SysAnnouncement> result = announcementMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(SysAnnouncement::getStatus, "enabled")
                        .like(StringUtils.hasText(keyword), SysAnnouncement::getTitle, keyword)
                        .orderByDesc(SysAnnouncement::getPublishAt));
        result.getRecords().forEach(this::sanitize);
        return ApiResponse.ok(result);
    }

    @GetMapping("/public/announcements/{id}")
    public ApiResponse<SysAnnouncement> publicDetail(@PathVariable Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null || !"enabled".equals(announcement.getStatus())) {
            throw new BusinessException(404, "公告不存在");
        }
        announcement.setViewCount(announcement.getViewCount() == null ? 1 : announcement.getViewCount() + 1);
        announcementMapper.updateById(announcement);
        sanitize(announcement);
        return ApiResponse.ok(announcement);
    }

    @GetMapping("/admin/announcements")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<SysAnnouncement>> adminList(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(announcementMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysAnnouncement>().orderByDesc(SysAnnouncement::getCreatedAt)));
    }

    @PostMapping("/admin/announcements")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SysAnnouncement> create(@RequestBody SysAnnouncement announcement) {
        sanitize(announcement);
        announcement.setPublisherId(SecurityUtils.currentUserId());
        if (announcement.getPublishAt() == null) {
            announcement.setPublishAt(LocalDateTime.now());
        }
        if (announcement.getViewCount() == null) {
            announcement.setViewCount(0L);
        }
        announcementMapper.insert(announcement);
        return ApiResponse.ok(announcement);
    }

    @PutMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysAnnouncement announcement) {
        sanitize(announcement);
        announcement.setId(id);
        announcementMapper.updateById(announcement);
        return ApiResponse.ok();
    }

    @DeleteMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void sanitize(SysAnnouncement announcement) {
        announcement.setTitle(announcement.getTitle() == null ? null : announcement.getTitle().strip());
        announcement.setContent(HtmlSecurity.announcement(announcement.getContent()));
    }
}
