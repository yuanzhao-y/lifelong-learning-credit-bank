package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.BizAuditRecord;
import com.zhousheng.llcb.entity.CertApplication;
import com.zhousheng.llcb.mapper.BizAuditRecordMapper;
import com.zhousheng.llcb.mapper.CertApplicationMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.CertificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final CertApplicationMapper applicationMapper;
    private final BizAuditRecordMapper auditRecordMapper;

    public CertificationController(CertificationService certificationService,
                                   CertApplicationMapper applicationMapper,
                                   BizAuditRecordMapper auditRecordMapper) {
        this.certificationService = certificationService;
        this.applicationMapper = applicationMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('learner','admin')")
    public ApiResponse<CertApplication> submit(@Valid @RequestBody BizDtos.CertSubmitRequest request) {
        return ApiResponse.ok(certificationService.submit(request));
    }

    @GetMapping("/mine")
    public ApiResponse<Page<CertApplication>> mine(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String status) {
        return ApiResponse.ok(applicationMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<CertApplication>()
                        .eq(CertApplication::getApplicantId, SecurityUtils.currentUserId())
                        .eq(StringUtils.hasText(status), CertApplication::getStatus, status)
                        .orderByDesc(CertApplication::getSubmittedAt)));
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable Long id) {
        certificationService.withdraw(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/audit-records")
    public ApiResponse<List<BizAuditRecord>> auditRecords(@PathVariable Long id) {
        return ApiResponse.ok(auditRecordMapper.selectList(new LambdaQueryWrapper<BizAuditRecord>()
                .eq(BizAuditRecord::getBizType, "cert_application")
                .eq(BizAuditRecord::getBizId, id)
                .orderByAsc(BizAuditRecord::getOperatedAt)));
    }

    @GetMapping("/audit/pending")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Page<CertApplication>> pending(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) String certifyType,
                                                      @RequestParam(required = false) Long applicantId) {
        return ApiResponse.ok(applicationMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<CertApplication>()
                        .eq(CertApplication::getStatus, "pending")
                        .eq(StringUtils.hasText(certifyType), CertApplication::getCertifyType, certifyType)
                        .eq(applicantId != null, CertApplication::getApplicantId, applicantId)
                        .orderByAsc(CertApplication::getSubmittedAt)));
    }

    @PostMapping("/audit/{id}/approve")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody(required = false) BizDtos.AuditRequest request) {
        certificationService.approve(id, request == null ? null : request.recognizedCredit());
        return ApiResponse.ok();
    }

    @PostMapping("/audit/{id}/reject")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody BizDtos.AuditRequest request) {
        certificationService.reject(id, request.reason());
        return ApiResponse.ok();
    }

    @PostMapping("/audit/batch-approve")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Void> batchApprove(@RequestBody AdminSystemController.IdsRequest request) {
        certificationService.batchApprove(request.ids());
        return ApiResponse.ok();
    }
}
