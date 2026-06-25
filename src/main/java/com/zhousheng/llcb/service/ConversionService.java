package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversionService {

    private final ConversionApplicationMapper applicationMapper;
    private final ConversionRuleMapper ruleMapper;
    private final LearnerOutcomeMapper outcomeMapper;
    private final ConversionTransactionMapper transactionMapper;
    private final CreditService creditService;
    private final AuditTrailService auditTrailService;
    private final MessageService messageService;

    public ConversionService(ConversionApplicationMapper applicationMapper,
                             ConversionRuleMapper ruleMapper,
                             LearnerOutcomeMapper outcomeMapper,
                             ConversionTransactionMapper transactionMapper,
                             CreditService creditService,
                             AuditTrailService auditTrailService,
                             MessageService messageService) {
        this.applicationMapper = applicationMapper;
        this.ruleMapper = ruleMapper;
        this.outcomeMapper = outcomeMapper;
        this.transactionMapper = transactionMapper;
        this.creditService = creditService;
        this.auditTrailService = auditTrailService;
        this.messageService = messageService;
    }

    public List<ConversionRule> matchRules(Long sourceOutcomeId) {
        LearnerOutcome source = ownOutcome(sourceOutcomeId);
        return ruleMapper.selectList(new LambdaQueryWrapper<ConversionRule>()
                .eq(ConversionRule::getSourceCatalogId, source.getCatalogId())
                .eq(ConversionRule::getStatus, Constants.STATUS_EFFECTIVE)
                .eq(ConversionRule::getEnabled, 1));
    }

    public BizDtos.ConversionPreviewResponse preview(Long ruleId, Long sourceOutcomeId, BigDecimal sourceCredit) {
        requirePositive(sourceCredit);
        LearnerOutcome source = ownOutcome(sourceOutcomeId);
        ConversionRule rule = usableRule(ruleId);
        if (!rule.getSourceCatalogId().equals(source.getCatalogId())) {
            throw new BusinessException("规则与源成果不匹配");
        }
        if (source.getAvailableCredit().compareTo(sourceCredit) < 0) {
            throw new BusinessException("源成果可用学分不足");
        }
        BigDecimal targetCredit = sourceCredit.multiply(rule.getConversionRatio()).setScale(2, RoundingMode.HALF_UP);
        return new BizDtos.ConversionPreviewResponse(rule.getId(), rule.getConversionRatio(), targetCredit);
    }

    @Transactional
    public ConversionApplication submit(BizDtos.ConversionSubmitRequest request) {
        requirePositive(request.sourceCredit());
        Long userId = SecurityUtils.currentUserId();
        LearnerOutcome source = ownOutcome(request.sourceOutcomeId());
        ConversionRule rule = usableRule(request.ruleId());
        if (!rule.getSourceCatalogId().equals(source.getCatalogId())) {
            throw new BusinessException("规则与源成果不匹配");
        }
        BigDecimal targetCredit = request.sourceCredit().multiply(rule.getConversionRatio()).setScale(2, RoundingMode.HALF_UP);
        ConversionApplication application = new ConversionApplication();
        application.setApplicationNo(BizNoGenerator.next("TA"));
        application.setApplicantId(userId);
        application.setRuleId(rule.getId());
        application.setSourceOutcomeId(source.getId());
        application.setSourceCatalogId(rule.getSourceCatalogId());
        application.setTargetCatalogId(rule.getTargetCatalogId());
        application.setSourceCredit(request.sourceCredit());
        application.setConversionRatio(rule.getConversionRatio());
        application.setTargetCredit(targetCredit);
        application.setStatus(Constants.STATUS_PENDING);
        application.setSubmittedAt(LocalDateTime.now());
        applicationMapper.insert(application);

        CreditFlow freezeFlow = creditService.freeze(source, request.sourceCredit(),
                "conversion_application", application.getId(), "成果转换申请冻结源学分");
        application.setFreezeFlowId(freezeFlow.getId());
        applicationMapper.updateById(application);
        auditTrailService.record("conversion_application", application.getId(), "submit", null, Constants.STATUS_PENDING, null);
        return application;
    }

    @Transactional
    public void approve(Long applicationId) {
        ConversionApplication application = claimPending(applicationId);
        LearnerOutcome source = outcomeMapper.selectById(application.getSourceOutcomeId());
        String from = application.getStatus();
        CreditFlow deductFlow = creditService.deductFrozen(source, application.getSourceCredit(),
                "conversion_application", application.getId(), "转换审核通过扣减源学分");

        LearnerOutcome target = new LearnerOutcome();
        target.setUserId(application.getApplicantId());
        target.setCatalogId(application.getTargetCatalogId());
        target.setSourceType("conversion");
        target.setSourceApplicationId(application.getId());
        target.setOutcomeName("转换成果-" + application.getApplicationNo());
        target.setTotalCredit(application.getTargetCredit());
        target.setAvailableCredit(application.getTargetCredit());
        target.setFrozenCredit(BigDecimal.ZERO);
        target.setStatus(Constants.STATUS_VALID);
        target.setCertifiedAt(LocalDateTime.now());
        outcomeMapper.insert(target);

        CreditFlow earnFlow = creditService.earn(application.getApplicantId(), target.getId(), application.getTargetCredit(),
                "conversion_application", application.getId(), "转换审核通过增加目标学分");
        application.setStatus(Constants.STATUS_APPROVED);
        application.setTargetOutcomeId(target.getId());
        application.setAuditUserId(SecurityUtils.currentUserId());
        application.setAuditedAt(LocalDateTime.now());
        applicationMapper.updateById(application);

        ConversionTransaction transaction = new ConversionTransaction();
        transaction.setTransactionNo(BizNoGenerator.next("CT"));
        transaction.setApplicationId(application.getId());
        transaction.setUserId(application.getApplicantId());
        transaction.setSourceOutcomeId(application.getSourceOutcomeId());
        transaction.setTargetOutcomeId(target.getId());
        transaction.setDeductFlowId(deductFlow.getId());
        transaction.setEarnFlowId(earnFlow.getId());
        transaction.setSourceCredit(application.getSourceCredit());
        transaction.setTargetCredit(application.getTargetCredit());
        transaction.setConvertedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        auditTrailService.record("conversion_application", applicationId, "approve", from, Constants.STATUS_APPROVED, null);
        messageService.send(application.getApplicantId(), "转换申请已通过",
                "您的成果转换申请 " + application.getApplicationNo() + " 已通过，目标学分 " + application.getTargetCredit() + "。",
                "conversion_result", "conversion_approved", "conversion_application", applicationId);
    }

    @Transactional
    public void reject(Long applicationId, String reason) {
        ConversionApplication application = claimPending(applicationId);
        LearnerOutcome source = outcomeMapper.selectById(application.getSourceOutcomeId());
        creditService.unfreeze(source, application.getSourceCredit(),
                "conversion_application", application.getId(), "转换审核驳回解冻源学分");
        String from = application.getStatus();
        application.setStatus(Constants.STATUS_REJECTED);
        application.setAuditUserId(SecurityUtils.currentUserId());
        application.setAuditedAt(LocalDateTime.now());
        application.setRejectReason(reason);
        applicationMapper.updateById(application);
        auditTrailService.record("conversion_application", applicationId, "reject", from, Constants.STATUS_REJECTED, reason);
        messageService.send(application.getApplicantId(), "转换申请被驳回",
                "您的成果转换申请 " + application.getApplicationNo() + " 被驳回，原因：" + reason,
                "conversion_result", "conversion_rejected", "conversion_application", applicationId);
    }

    private LearnerOutcome ownOutcome(Long outcomeId) {
        LearnerOutcome outcome = outcomeMapper.selectById(outcomeId);
        if (outcome == null || !SecurityUtils.currentUserId().equals(outcome.getUserId())) {
            throw new BusinessException("源成果不存在");
        }
        if (!Constants.STATUS_VALID.equals(outcome.getStatus())) {
            throw new BusinessException("源成果不可用");
        }
        return outcome;
    }

    private ConversionRule usableRule(Long ruleId) {
        ConversionRule rule = ruleMapper.selectById(ruleId);
        if (rule == null || !Constants.STATUS_EFFECTIVE.equals(rule.getStatus()) || rule.getEnabled() == null || rule.getEnabled() != 1) {
            throw new BusinessException("转换规则不存在或未生效");
        }
        return rule;
    }

    private ConversionApplication claimPending(Long applicationId) {
        ConversionApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("转换申请不存在");
        }
        int updated = applicationMapper.update(null, new UpdateWrapper<ConversionApplication>()
                .eq("id", applicationId)
                .eq("status", Constants.STATUS_PENDING)
                .set("status", "processing")
                .set("audit_user_id", SecurityUtils.currentUserId())
                .set("audited_at", LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException("转换申请已被其他审核员处理");
        }
        return application;
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("源学分必须大于 0");
        }
    }
}
