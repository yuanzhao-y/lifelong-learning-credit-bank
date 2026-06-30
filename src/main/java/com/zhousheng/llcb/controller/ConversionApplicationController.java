package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.BizAuditRecord;
import com.zhousheng.llcb.entity.ConversionApplication;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.BizAuditRecordMapper;
import com.zhousheng.llcb.mapper.ConversionApplicationMapper;
import com.zhousheng.llcb.mapper.ConversionRuleMapper;
import com.zhousheng.llcb.mapper.LearnOutcomeCatalogMapper;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.ConversionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/conversions")
public class ConversionApplicationController {

    private final ConversionService conversionService;
    private final ConversionApplicationMapper applicationMapper;
    private final BizAuditRecordMapper auditRecordMapper;
    private final SysUserMapper userMapper;
    private final ConversionRuleMapper ruleMapper;
    private final LearnerOutcomeMapper outcomeMapper;
    private final LearnOutcomeCatalogMapper catalogMapper;

    public ConversionApplicationController(ConversionService conversionService,
                                           ConversionApplicationMapper applicationMapper,
                                           BizAuditRecordMapper auditRecordMapper,
                                           SysUserMapper userMapper,
                                           ConversionRuleMapper ruleMapper,
                                           LearnerOutcomeMapper outcomeMapper,
                                           LearnOutcomeCatalogMapper catalogMapper) {
        this.conversionService = conversionService;
        this.applicationMapper = applicationMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.userMapper = userMapper;
        this.ruleMapper = ruleMapper;
        this.outcomeMapper = outcomeMapper;
        this.catalogMapper = catalogMapper;
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
        assertCanViewApplication(id);
        return ApiResponse.ok(auditRecordMapper.selectList(new LambdaQueryWrapper<BizAuditRecord>()
                .eq(BizAuditRecord::getBizType, "conversion_application")
                .eq(BizAuditRecord::getBizId, id)
                .orderByAsc(BizAuditRecord::getOperatedAt)));
    }

    @GetMapping("/audit/pending")
    @PreAuthorize("hasAnyRole('auditor','admin')")
    public ApiResponse<Page<BizDtos.ConversionAuditItem>> pending(@RequestParam(defaultValue = "1") long page,
                                                                  @RequestParam(defaultValue = "10") long size,
                                                                  @RequestParam(required = false) Long ruleId,
                                                                  @RequestParam(required = false) Long applicantId) {
        Page<ConversionApplication> applications = applicationMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<ConversionApplication>()
                        .eq(ConversionApplication::getStatus, "pending")
                        .eq(ruleId != null, ConversionApplication::getRuleId, ruleId)
                        .eq(applicantId != null, ConversionApplication::getApplicantId, applicantId)
                        .orderByAsc(ConversionApplication::getSubmittedAt));
        return ApiResponse.ok(toAuditPage(applications));
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

    private void assertCanViewApplication(Long id) {
        if (SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("auditor")) {
            return;
        }
        ConversionApplication application = applicationMapper.selectById(id);
        if (application == null || !SecurityUtils.currentUserId().equals(application.getApplicantId())) {
            throw new BusinessException(404, "Conversion application not found");
        }
    }

    private Page<BizDtos.ConversionAuditItem> toAuditPage(Page<ConversionApplication> applications) {
        Page<BizDtos.ConversionAuditItem> result = MybatisPlusConfig.page(applications.getCurrent(), applications.getSize());
        result.setTotal(applications.getTotal());
        List<ConversionApplication> records = applications.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(List.of());
            return result;
        }

        Map<Long, SysUser> users = users(records);
        Map<Long, ConversionRule> rules = rules(records);
        Map<Long, LearnerOutcome> outcomes = outcomes(records);
        Map<Long, LearnOutcomeCatalog> catalogs = catalogs(records);

        result.setRecords(records.stream()
                .map(item -> toAuditItem(item, users, rules, outcomes, catalogs))
                .toList());
        return result;
    }

    private BizDtos.ConversionAuditItem toAuditItem(ConversionApplication item,
                                                    Map<Long, SysUser> users,
                                                    Map<Long, ConversionRule> rules,
                                                    Map<Long, LearnerOutcome> outcomes,
                                                    Map<Long, LearnOutcomeCatalog> catalogs) {
        SysUser user = users.get(item.getApplicantId());
        ConversionRule rule = rules.get(item.getRuleId());
        LearnerOutcome sourceOutcome = outcomes.get(item.getSourceOutcomeId());
        LearnOutcomeCatalog sourceCatalog = catalogs.get(item.getSourceCatalogId());
        LearnOutcomeCatalog targetCatalog = catalogs.get(item.getTargetCatalogId());
        return new BizDtos.ConversionAuditItem(
                item.getId(),
                item.getApplicationNo(),
                item.getApplicantId(),
                user == null ? null : user.getRealName(),
                item.getRuleId(),
                rule == null ? null : rule.getRuleName(),
                item.getSourceOutcomeId(),
                sourceOutcome == null ? null : sourceOutcome.getOutcomeName(),
                item.getSourceCatalogId(),
                sourceCatalog == null ? null : sourceCatalog.getOutcomeName(),
                item.getTargetCatalogId(),
                targetCatalog == null ? null : targetCatalog.getOutcomeName(),
                item.getSourceCredit(),
                item.getConversionRatio(),
                item.getTargetCredit(),
                item.getStatus(),
                item.getFreezeFlowId(),
                item.getTargetOutcomeId(),
                item.getSubmittedAt(),
                item.getAuditUserId(),
                item.getAuditedAt(),
                item.getRejectReason());
    }

    private Map<Long, SysUser> users(List<ConversionApplication> records) {
        Set<Long> ids = records.stream()
                .map(ConversionApplication::getApplicantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, ConversionRule> rules(List<ConversionApplication> records) {
        Set<Long> ids = records.stream()
                .map(ConversionApplication::getRuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return ruleMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ConversionRule::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, LearnerOutcome> outcomes(List<ConversionApplication> records) {
        Set<Long> ids = records.stream()
                .map(ConversionApplication::getSourceOutcomeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return outcomeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(LearnerOutcome::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, LearnOutcomeCatalog> catalogs(List<ConversionApplication> records) {
        Set<Long> ids = records.stream()
                .flatMap(item -> Stream.of(item.getSourceCatalogId(), item.getTargetCatalogId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return catalogMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(LearnOutcomeCatalog::getId, Function.identity(), (left, right) -> left));
    }
}
