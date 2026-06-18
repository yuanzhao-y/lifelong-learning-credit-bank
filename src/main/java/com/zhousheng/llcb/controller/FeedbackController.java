package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.SysFeedback;
import com.zhousheng.llcb.mapper.SysFeedbackMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.BizNoGenerator;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class FeedbackController {

    private final SysFeedbackMapper feedbackMapper;

    public FeedbackController(SysFeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @PostMapping("/feedback")
    public ApiResponse<SysFeedback> submit(@RequestBody SysFeedback feedback) {
        feedback.setFeedbackNo(BizNoGenerator.next("FB"));
        feedback.setUserId(SecurityUtils.currentUserId());
        feedback.setStatus("pending");
        feedbackMapper.insert(feedback);
        return ApiResponse.ok(feedback);
    }

    @GetMapping("/feedback/mine")
    public ApiResponse<Page<SysFeedback>> mine(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(feedbackMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysFeedback>()
                        .eq(SysFeedback::getUserId, SecurityUtils.currentUserId())
                        .orderByDesc(SysFeedback::getCreatedAt)));
    }

    @GetMapping("/admin/feedback")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<SysFeedback>> adminList(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) String status) {
        return ApiResponse.ok(feedbackMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysFeedback>()
                        .eq(StringUtils.hasText(status), SysFeedback::getStatus, status)
                        .orderByDesc(SysFeedback::getCreatedAt)));
    }

    @PostMapping("/admin/feedback/{id}/reply")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> reply(@PathVariable Long id, @Valid @RequestBody BizDtos.FeedbackReplyRequest request) {
        SysFeedback feedback = feedbackMapper.selectById(id);
        feedback.setReplyContent(request.replyContent());
        feedback.setReplyAdminId(SecurityUtils.currentUserId());
        feedback.setRepliedAt(LocalDateTime.now());
        feedback.setStatus("processed");
        feedbackMapper.updateById(feedback);
        return ApiResponse.ok();
    }
}
