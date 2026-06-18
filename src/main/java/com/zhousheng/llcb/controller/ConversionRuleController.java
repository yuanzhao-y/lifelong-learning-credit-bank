package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.entity.ConversionRuleReview;
import com.zhousheng.llcb.entity.ExpertProfile;
import com.zhousheng.llcb.mapper.ConversionRuleMapper;
import com.zhousheng.llcb.mapper.ConversionRuleReviewMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.ConversionRuleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConversionRuleController {

    private final ConversionRuleMapper ruleMapper;
    private final ConversionRuleReviewMapper reviewMapper;
    private final ConversionRuleService ruleService;

    public ConversionRuleController(ConversionRuleMapper ruleMapper,
                                    ConversionRuleReviewMapper reviewMapper,
                                    ConversionRuleService ruleService) {
        this.ruleMapper = ruleMapper;
        this.reviewMapper = reviewMapper;
        this.ruleService = ruleService;
    }

    @GetMapping("/public/conversion-rules")
    public ApiResponse<Page<ConversionRule>> publicRules(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size,
                                                         @RequestParam(required = false) Long sourceCatalogId,
                                                         @RequestParam(required = false) Long targetCatalogId) {
        return ApiResponse.ok(ruleMapper.selectPage(MybatisPlusConfig.page(page, size), ruleQuery(Constants.STATUS_EFFECTIVE, sourceCatalogId, targetCatalogId)));
    }

    @GetMapping("/public/conversion-rules/{id}")
    public ApiResponse<ConversionRule> publicDetail(@PathVariable Long id) {
        return ApiResponse.ok(ruleMapper.selectById(id));
    }

    @GetMapping("/conversion-rules")
    public ApiResponse<Page<ConversionRule>> rules(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long sourceCatalogId,
                                                   @RequestParam(required = false) Long targetCatalogId) {
        return ApiResponse.ok(ruleMapper.selectPage(MybatisPlusConfig.page(page, size), ruleQuery(status, sourceCatalogId, targetCatalogId)));
    }

    @PostMapping("/conversion-rules")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ConversionRule> create(@RequestBody ConversionRule rule) {
        if (!StringUtils.hasText(rule.getStatus())) {
            rule.setStatus("draft");
        }
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        ruleMapper.insert(rule);
        return ApiResponse.ok(rule);
    }

    @PutMapping("/conversion-rules/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ConversionRule rule) {
        rule.setId(id);
        ruleMapper.updateById(rule);
        return ApiResponse.ok();
    }

    @DeleteMapping("/conversion-rules/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ruleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PostMapping("/conversion-rules/{id}/submit-review")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> submitReview(@PathVariable Long id) {
        ruleService.submitReview(id);
        return ApiResponse.ok();
    }

    @PostMapping("/conversion-rules/{id}/assign-expert")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ConversionRuleReview> assignExpert(@PathVariable Long id, @Valid @RequestBody BizDtos.ReviewAssignRequest request) {
        return ApiResponse.ok(ruleService.assignExpert(id, request));
    }

    @GetMapping("/experts/me")
    @PreAuthorize("hasRole('expert')")
    public ApiResponse<ExpertProfile> currentExpert() {
        return ApiResponse.ok(ruleService.currentExpertProfile());
    }

    @GetMapping("/experts/reviews")
    @PreAuthorize("hasRole('expert')")
    public ApiResponse<List<ConversionRuleReview>> myReviews() {
        ExpertProfile expert = ruleService.currentExpertProfile();
        return ApiResponse.ok(reviewMapper.selectList(new LambdaQueryWrapper<ConversionRuleReview>()
                .eq(ConversionRuleReview::getExpertId, expert.getId())
                .orderByDesc(ConversionRuleReview::getAssignedAt)));
    }

    @PostMapping("/experts/reviews/{id}")
    @PreAuthorize("hasRole('expert')")
    public ApiResponse<Void> review(@PathVariable Long id, @Valid @RequestBody BizDtos.ExpertReviewRequest request) {
        ruleService.review(id, request);
        return ApiResponse.ok();
    }

    private LambdaQueryWrapper<ConversionRule> ruleQuery(String status, Long sourceCatalogId, Long targetCatalogId) {
        return new LambdaQueryWrapper<ConversionRule>()
                .eq(StringUtils.hasText(status), ConversionRule::getStatus, status)
                .eq(sourceCatalogId != null, ConversionRule::getSourceCatalogId, sourceCatalogId)
                .eq(targetCatalogId != null, ConversionRule::getTargetCatalogId, targetCatalogId)
                .orderByDesc(ConversionRule::getCreatedAt);
    }
}
