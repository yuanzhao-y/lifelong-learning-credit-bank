package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationService {

    private final CertApplicationMapper applicationMapper;
    private final LearnOutcomeCatalogMapper catalogMapper;
    private final LearnerOutcomeMapper learnerOutcomeMapper;
    private final SysFileMapper fileMapper;
    private final CreditService creditService;
    private final AuditTrailService auditTrailService;
    private final MessageService messageService;

    public CertificationService(CertApplicationMapper applicationMapper,
                                LearnOutcomeCatalogMapper catalogMapper,
                                LearnerOutcomeMapper learnerOutcomeMapper,
                                SysFileMapper fileMapper,
                                CreditService creditService,
                                AuditTrailService auditTrailService,
                                MessageService messageService) {
        this.applicationMapper = applicationMapper;
        this.catalogMapper = catalogMapper;
        this.learnerOutcomeMapper = learnerOutcomeMapper;
        this.fileMapper = fileMapper;
        this.creditService = creditService;
        this.auditTrailService = auditTrailService;
        this.messageService = messageService;
    }

    @Transactional
    public CertApplication submit(BizDtos.CertSubmitRequest request) {
        Long userId = SecurityUtils.currentUserId();
        LearnOutcomeCatalog catalog = catalogMapper.selectById(request.catalogId());
        if (catalog == null || !Constants.STATUS_ENABLED.equals(catalog.getStatus())) {
            throw new BusinessException("成果目录不存在或已停用");
        }
        CertApplication application = new CertApplication();
        application.setApplicationNo(BizNoGenerator.next("CA"));
        application.setApplicantId(userId);
        application.setCatalogId(catalog.getId());
        application.setCertifyType(request.certifyType());
        application.setOutcomeName(request.outcomeName());
        application.setCertificateNo(request.certificateNo());
        application.setIssuingAuthority(request.issuingAuthority());
        application.setObtainedAt(request.obtainedAt());
        application.setRequestedCredit(request.requestedCredit() == null ? catalog.getBaseCredit() : request.requestedCredit());
        application.setStatus(Constants.STATUS_PENDING);
        application.setSubmittedAt(LocalDateTime.now());
        applicationMapper.insert(application);

        if (request.materialFileIds() != null && !request.materialFileIds().isEmpty()) {
            fileMapper.update(null, new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getId, request.materialFileIds())
                    .set(SysFile::getBizType, "cert_application")
                    .set(SysFile::getBizId, application.getId()));
        }
        auditTrailService.record("cert_application", application.getId(), "submit", null, Constants.STATUS_PENDING, null);
        return application;
    }

    @Transactional
    public void withdraw(Long applicationId) {
        CertApplication application = ownApplication(applicationId);
        if (!Constants.STATUS_PENDING.equals(application.getStatus())) {
            throw new BusinessException("只有待审核申请可以撤回");
        }
        String from = application.getStatus();
        application.setStatus(Constants.STATUS_WITHDRAWN);
        application.setWithdrawnAt(LocalDateTime.now());
        applicationMapper.updateById(application);
        auditTrailService.record("cert_application", applicationId, "withdraw", from, Constants.STATUS_WITHDRAWN, null);
    }

    @Transactional
    public void approve(Long applicationId, BigDecimal recognizedCredit) {
        CertApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!Constants.STATUS_PENDING.equals(application.getStatus())) {
            throw new BusinessException("申请已处理");
        }
        BigDecimal credit = recognizedCredit != null ? recognizedCredit : application.getRequestedCredit();
        if (credit == null || credit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("认定学分必须大于 0");
        }
        String from = application.getStatus();
        application.setRecognizedCredit(credit);
        application.setStatus(Constants.STATUS_APPROVED);
        application.setAuditUserId(SecurityUtils.currentUserId());
        application.setAuditedAt(LocalDateTime.now());
        applicationMapper.updateById(application);

        LearnerOutcome outcome = new LearnerOutcome();
        outcome.setUserId(application.getApplicantId());
        outcome.setCatalogId(application.getCatalogId());
        outcome.setSourceType("certification");
        outcome.setSourceApplicationId(application.getId());
        outcome.setOutcomeName(application.getOutcomeName());
        outcome.setCertificateNo(application.getCertificateNo());
        outcome.setIssuingAuthority(application.getIssuingAuthority());
        outcome.setObtainedAt(application.getObtainedAt());
        outcome.setTotalCredit(credit);
        outcome.setAvailableCredit(credit);
        outcome.setFrozenCredit(BigDecimal.ZERO);
        outcome.setStatus(Constants.STATUS_VALID);
        outcome.setCertifiedAt(LocalDateTime.now());
        learnerOutcomeMapper.insert(outcome);

        creditService.earn(application.getApplicantId(), outcome.getId(), credit,
                "cert_application", application.getId(), "学习成果认证通过");
        auditTrailService.record("cert_application", applicationId, "approve", from, Constants.STATUS_APPROVED, null);
        messageService.send(application.getApplicantId(), "认证申请已通过",
                "您的学习成果认证申请 " + application.getApplicationNo() + " 已通过，认定学分 " + credit + "。",
                "cert_result", "cert_approved", "cert_application", applicationId);
    }

    @Transactional
    public void reject(Long applicationId, String reason) {
        CertApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!Constants.STATUS_PENDING.equals(application.getStatus())) {
            throw new BusinessException("申请已处理");
        }
        String from = application.getStatus();
        application.setStatus(Constants.STATUS_REJECTED);
        application.setAuditUserId(SecurityUtils.currentUserId());
        application.setAuditedAt(LocalDateTime.now());
        application.setRejectReason(reason);
        applicationMapper.updateById(application);
        auditTrailService.record("cert_application", applicationId, "reject", from, Constants.STATUS_REJECTED, reason);
        messageService.send(application.getApplicantId(), "认证申请被驳回",
                "您的学习成果认证申请 " + application.getApplicationNo() + " 被驳回，原因：" + reason,
                "cert_result", "cert_rejected", "cert_application", applicationId);
    }

    @Transactional
    public void batchApprove(List<Long> ids) {
        for (Long id : ids) {
            approve(id, null);
        }
    }

    private CertApplication ownApplication(Long applicationId) {
        CertApplication application = applicationMapper.selectById(applicationId);
        if (application == null || !SecurityUtils.currentUserId().equals(application.getApplicantId())) {
            throw new BusinessException("申请不存在");
        }
        return application;
    }
}
