package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.BizAuditRecord;
import com.zhousheng.llcb.entity.ConversionApplication;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.mapper.BizAuditRecordMapper;
import com.zhousheng.llcb.mapper.ConversionApplicationMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.ConversionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/conversions")
public class ConversionApplicationController {

    private final ConversionService conversionService;
    private final ConversionApplicationMapper applicationMapper;
    private final BizAuditRecordMapper auditRecordMapper;

    public ConversionApplicationController(ConversionService conversionService,
                                           ConversionApplicationMapper applicationMapper,
                                           BizAuditRecordMapper auditRecordMapper) {
        this.conversionService = conversionService;
        this.applicationMapper = applicationMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    @GetMapping("/match-rules")
    public ApiResponse<List<ConversionRule>> matchRules(@RequestParam Long sourceOutcomeId) {
        return ApiResponse.ok(conversionService.matchRules(sourceOutcomeId));
    }

    @GetMapping("/preview")
    public ApiResponse<BizDtos.ConversionPreviewResponse> preview(@RequestParam Long ruleId,
                                                                  @RequestParam Long sourceOutcomeId,
                                                                  @RequestParam BigDecimal sourceCredit) {
        return ApiResponse.ok(conversionService.preview(ruleId, sourceOutcomeId, sourceCredit));
    }

    @PostMapping
    public ApiResponse<ConversionApplication> submit(@Valid @RequestBody BizDtos.ConversionSubmitRequest request) {
        return ApiResponse.ok(conversionService.submit(request));
    }

    @GetMapping("/mine")
    public ApiResponse<Page<ConversionApplication>> mine(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size,
                                                         @RequestParam(required = false) String status) {
        return ApiResponse.ok(applicationMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<ConversionApplication>()
                        .eq(ConversionApplication::getApplicantId, SecurityUtils.currentUserId())
                        .eq(StringUtils.hasText(status), ConversionApplication::getStatus, status)
                        .orderByDesc(ConversionApplication::getSubmittedAt)));
    }

    @GetMapping("/{id}/audit-records")
    public ApiResponse<List<BizAuditRecord>> auditRecords(@PathVariable Long id) {
        return ApiResponse.ok(auditRecordMapper.selectList(new LambdaQueryWrapper<BizAuditRecord>()
                .eq(BizAuditRecord::getBizType, "conversion_application")
                .eq(BizAuditRecord::getBizId, id)
                .orderByAsc(BizAuditRecord::getOperatedAt)));
    }

    @GetMapping("/audit/pending")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Page<ConversionApplication>> pending(@RequestParam(defaultValue = "1") long page,
                                                            @RequestParam(defaultValue = "10") long size,
                                                            @RequestParam(required = false) Long ruleId,
                                                            @RequestParam(required = false) Long applicantId) {
        return ApiResponse.ok(applicationMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<ConversionApplication>()
                        .eq(ConversionApplication::getStatus, "pending")
                        .eq(ruleId != null, ConversionApplication::getRuleId, ruleId)
                        .eq(applicantId != null, ConversionApplication::getApplicantId, applicantId)
                        .orderByAsc(ConversionApplication::getSubmittedAt)));
    }

    @PostMapping("/audit/{id}/approve")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        conversionService.approve(id);
        return ApiResponse.ok();
    }

    @PostMapping("/audit/{id}/reject")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody BizDtos.AuditRequest request) {
        conversionService.reject(id, request.reason());
        return ApiResponse.ok();
    }
}
