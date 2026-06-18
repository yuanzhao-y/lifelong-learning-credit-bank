package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.entity.ConversionRuleReview;
import com.zhousheng.llcb.entity.ExpertProfile;
import com.zhousheng.llcb.mapper.ConversionRuleMapper;
import com.zhousheng.llcb.mapper.ConversionRuleReviewMapper;
import com.zhousheng.llcb.mapper.ExpertProfileMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConversionRuleService {

    private final ConversionRuleMapper ruleMapper;
    private final ConversionRuleReviewMapper reviewMapper;
    private final ExpertProfileMapper expertMapper;
    private final AuditTrailService auditTrailService;
    private final MessageService messageService;

    public ConversionRuleService(ConversionRuleMapper ruleMapper,
                                 ConversionRuleReviewMapper reviewMapper,
                                 ExpertProfileMapper expertMapper,
                                 AuditTrailService auditTrailService,
                                 MessageService messageService) {
        this.ruleMapper = ruleMapper;
        this.reviewMapper = reviewMapper;
        this.expertMapper = expertMapper;
        this.auditTrailService = auditTrailService;
        this.messageService = messageService;
    }

    @Transactional
    public void submitReview(Long ruleId) {
        ConversionRule rule = requiredRule(ruleId);
        if (!"draft".equals(rule.getStatus())) {
            throw new BusinessException("只有草稿规则可以提交评审");
        }
        String from = rule.getStatus();
        rule.setStatus(Constants.STATUS_REVIEWING);
        rule.setSubmittedAt(LocalDateTime.now());
        ruleMapper.updateById(rule);
        auditTrailService.record("conversion_rule", ruleId, "submit", from, Constants.STATUS_REVIEWING, null);
    }

    @Transactional
    public ConversionRuleReview assignExpert(Long ruleId, BizDtos.ReviewAssignRequest request) {
        ConversionRule rule = requiredRule(ruleId);
        if (!Constants.STATUS_REVIEWING.equals(rule.getStatus())) {
            throw new BusinessException("规则未处于评审中");
        }
        ExpertProfile expert = expertMapper.selectById(request.expertId());
        if (expert == null || !"available".equals(expert.getStatus())) {
            throw new BusinessException("专家不存在或不可用");
        }
        ConversionRuleReview review = new ConversionRuleReview();
        review.setRuleId(ruleId);
        review.setExpertId(expert.getId());
        review.setAssignedBy(SecurityUtils.currentUserId());
        review.setAssignedAt(LocalDateTime.now());
        review.setStatus(Constants.STATUS_PENDING);
        reviewMapper.insert(review);
        auditTrailService.record("conversion_rule", ruleId, "assign", Constants.STATUS_REVIEWING, Constants.STATUS_REVIEWING,
                "指派专家: " + expert.getExpertName());
        if (expert.getUserId() != null) {
            messageService.send(expert.getUserId(), "新的转换规则评审任务",
                    "您有新的转换规则评审任务：" + rule.getRuleName(),
                    "expert_review", "rule_review_assigned", "conversion_rule", ruleId);
        }
        return review;
    }

    @Transactional
    public void review(Long reviewId, BizDtos.ExpertReviewRequest request) {
        ConversionRuleReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评审任务不存在");
        }
        ExpertProfile expert = expertMapper.selectById(review.getExpertId());
        Long currentUserId = SecurityUtils.currentUserId();
        if (expert == null || expert.getUserId() == null || !expert.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能处理自己的评审任务");
        }
        if (!Constants.STATUS_PENDING.equals(review.getStatus())) {
            throw new BusinessException("评审任务已处理");
        }
        review.setStatus("reviewed");
        review.setOpinion(request.opinion());
        review.setReviewComment(request.reviewComment());
        review.setReviewedAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        ConversionRule rule = requiredRule(review.getRuleId());
        if ("support".equals(request.opinion())) {
            String from = rule.getStatus();
            rule.setStatus(Constants.STATUS_EFFECTIVE);
            rule.setEffectiveAt(LocalDateTime.now());
            rule.setEnabled(1);
            ruleMapper.updateById(rule);
            auditTrailService.record("conversion_rule", rule.getId(), "review", from, Constants.STATUS_EFFECTIVE, request.reviewComment());
        } else {
            auditTrailService.record("conversion_rule", rule.getId(), "review", rule.getStatus(), rule.getStatus(), request.reviewComment());
        }
    }

    public ExpertProfile currentExpertProfile() {
        ExpertProfile expert = expertMapper.selectOne(new LambdaQueryWrapper<ExpertProfile>()
                .eq(ExpertProfile::getUserId, SecurityUtils.currentUserId())
                .last("limit 1"));
        if (expert == null) {
            throw new BusinessException("当前用户未绑定专家档案");
        }
        return expert;
    }

    private ConversionRule requiredRule(Long ruleId) {
        ConversionRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("转换规则不存在");
        }
        return rule;
    }
}
