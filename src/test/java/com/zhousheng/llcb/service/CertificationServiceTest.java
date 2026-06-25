package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.CertApplication;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.mapper.CertApplicationMapper;
import com.zhousheng.llcb.mapper.LearnOutcomeCatalogMapper;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import com.zhousheng.llcb.mapper.SysFileMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock
    private CertApplicationMapper applicationMapper;
    @Mock
    private LearnOutcomeCatalogMapper catalogMapper;
    @Mock
    private LearnerOutcomeMapper learnerOutcomeMapper;
    @Mock
    private SysFileMapper fileMapper;
    @Mock
    private CreditService creditService;
    @Mock
    private AuditTrailService auditTrailService;
    @Mock
    private MessageService messageService;

    private CertificationService service;

    @BeforeEach
    void setUp() {
        service = new CertificationService(applicationMapper, catalogMapper, learnerOutcomeMapper,
                fileMapper, creditService, auditTrailService, messageService);
        TestSecurity.loginAs(7L, "qa_learner", "learner");
    }

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    @Test
    void submitCreatesPendingApplicationForEnabledCatalog() {
        LearnOutcomeCatalog catalog = catalog(100L, "10.00", Constants.STATUS_ENABLED);
        when(catalogMapper.selectById(100L)).thenReturn(catalog);
        BizDtos.CertSubmitRequest request = new BizDtos.CertSubmitRequest(
                100L, "course_cert", "Software Course", "CERT-001", "School",
                LocalDate.of(2026, 1, 1), null, List.of());

        CertApplication result = service.submit(request);

        assertThat(result.getApplicantId()).isEqualTo(7L);
        assertThat(result.getStatus()).isEqualTo(Constants.STATUS_PENDING);
        assertThat(result.getRequestedCredit()).isEqualByComparingTo("10.00");
        verify(applicationMapper).insert(any(CertApplication.class));
        verify(fileMapper, never()).update(eq(null), any());
        verify(auditTrailService).record(eq("cert_application"), any(), eq("submit"),
                eq(null), eq(Constants.STATUS_PENDING), eq(null));
    }

    @Test
    void submitRejectsDisabledCatalog() {
        when(catalogMapper.selectById(100L)).thenReturn(catalog(100L, "10.00", Constants.STATUS_DISABLED));
        BizDtos.CertSubmitRequest request = new BizDtos.CertSubmitRequest(
                100L, "course_cert", "Software Course", null, null, null, null, List.of());

        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(BusinessException.class);

        verify(applicationMapper, never()).insert(any());
    }

    @Test
    void approveCreatesLearnerOutcomeAndEarnsCredit() {
        CertApplication application = pendingApplication(200L, 7L);
        when(applicationMapper.selectById(200L)).thenReturn(application);
        when(applicationMapper.update(any(), any())).thenReturn(1);

        service.approve(200L, new BigDecimal("12.50"));

        assertThat(application.getStatus()).isEqualTo(Constants.STATUS_APPROVED);
        assertThat(application.getRecognizedCredit()).isEqualByComparingTo("12.50");
        ArgumentCaptor<LearnerOutcome> outcomeCaptor = ArgumentCaptor.forClass(LearnerOutcome.class);
        verify(learnerOutcomeMapper).insert(outcomeCaptor.capture());
        assertThat(outcomeCaptor.getValue().getAvailableCredit()).isEqualByComparingTo("12.50");
        verify(creditService).earn(eq(7L), any(), eq(new BigDecimal("12.50")),
                eq("cert_application"), eq(200L), any());
        verify(messageService).send(eq(7L), any(), any(), eq("cert_result"),
                eq("cert_approved"), eq("cert_application"), eq(200L));
    }

    @Test
    void rejectMarksApplicationRejectedAndSendsMessage() {
        CertApplication application = pendingApplication(200L, 7L);
        when(applicationMapper.selectById(200L)).thenReturn(application);
        when(applicationMapper.update(any(), any())).thenReturn(1);

        service.reject(200L, "invalid material");

        assertThat(application.getStatus()).isEqualTo(Constants.STATUS_REJECTED);
        assertThat(application.getRejectReason()).isEqualTo("invalid material");
        verify(messageService).send(eq(7L), any(), any(), eq("cert_result"),
                eq("cert_rejected"), eq("cert_application"), eq(200L));
    }

    @Test
    void concurrentApprovalClaimFailureHasNoSideEffects() {
        when(applicationMapper.selectById(200L)).thenReturn(pendingApplication(200L, 7L));
        when(applicationMapper.update(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.approve(200L, new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("其他审核员");

        verify(learnerOutcomeMapper, never()).insert(any());
        verify(creditService, never()).earn(any(), any(), any(), any(), any(), any());
        verify(messageService, never()).send(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void withdrawClaimFailureDoesNotWriteAuditTrail() {
        when(applicationMapper.selectById(200L)).thenReturn(pendingApplication(200L, 7L));
        when(applicationMapper.update(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.withdraw(200L)).isInstanceOf(BusinessException.class);

        verify(auditTrailService, never()).record(any(), any(), any(), any(), any(), any());
    }

    private LearnOutcomeCatalog catalog(Long id, String baseCredit, String status) {
        LearnOutcomeCatalog catalog = new LearnOutcomeCatalog();
        catalog.setId(id);
        catalog.setBaseCredit(new BigDecimal(baseCredit));
        catalog.setStatus(status);
        return catalog;
    }

    private CertApplication pendingApplication(Long id, Long applicantId) {
        CertApplication application = new CertApplication();
        application.setId(id);
        application.setApplicantId(applicantId);
        application.setCatalogId(100L);
        application.setOutcomeName("Software Course");
        application.setCertificateNo("CERT-001");
        application.setIssuingAuthority("School");
        application.setRequestedCredit(new BigDecimal("10.00"));
        application.setStatus(Constants.STATUS_PENDING);
        return application;
    }
}
