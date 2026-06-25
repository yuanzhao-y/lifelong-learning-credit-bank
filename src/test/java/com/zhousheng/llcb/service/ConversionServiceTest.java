package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.ConversionApplication;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.entity.ConversionTransaction;
import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.mapper.ConversionApplicationMapper;
import com.zhousheng.llcb.mapper.ConversionRuleMapper;
import com.zhousheng.llcb.mapper.ConversionTransactionMapper;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private ConversionApplicationMapper applicationMapper;
    @Mock
    private ConversionRuleMapper ruleMapper;
    @Mock
    private LearnerOutcomeMapper outcomeMapper;
    @Mock
    private ConversionTransactionMapper transactionMapper;
    @Mock
    private CreditService creditService;
    @Mock
    private AuditTrailService auditTrailService;
    @Mock
    private MessageService messageService;

    private ConversionService service;

    @BeforeEach
    void setUp() {
        service = new ConversionService(applicationMapper, ruleMapper, outcomeMapper, transactionMapper,
                creditService, auditTrailService, messageService);
        TestSecurity.loginAs(7L, "qa_learner", "learner");
    }

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    @Test
    void previewCalculatesTargetCreditWithTwoDecimals() {
        when(outcomeMapper.selectById(11L)).thenReturn(outcome(11L, 7L, 100L, "10.00"));
        when(ruleMapper.selectById(22L)).thenReturn(rule(22L, 100L, 200L, "0.80"));

        BizDtos.ConversionPreviewResponse response = service.preview(22L, 11L, new BigDecimal("10.00"));

        assertThat(response.targetCredit()).isEqualByComparingTo("8.00");
        assertThat(response.conversionRatio()).isEqualByComparingTo("0.80");
    }

    @Test
    void previewRejectsMismatchedRule() {
        when(outcomeMapper.selectById(11L)).thenReturn(outcome(11L, 7L, 100L, "10.00"));
        when(ruleMapper.selectById(22L)).thenReturn(rule(22L, 999L, 200L, "0.80"));

        assertThatThrownBy(() -> service.preview(22L, 11L, new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void submitCreatesApplicationAndFreezesCredit() {
        LearnerOutcome source = outcome(11L, 7L, 100L, "10.00");
        ConversionRule rule = rule(22L, 100L, 200L, "0.80");
        CreditFlow freezeFlow = new CreditFlow();
        freezeFlow.setId(33L);
        when(outcomeMapper.selectById(11L)).thenReturn(source);
        when(ruleMapper.selectById(22L)).thenReturn(rule);
        when(creditService.freeze(eq(source), eq(new BigDecimal("5.00")), eq("conversion_application"), any(), any()))
                .thenReturn(freezeFlow);

        ConversionApplication application = service.submit(new BizDtos.ConversionSubmitRequest(22L, 11L, new BigDecimal("5.00")));

        assertThat(application.getApplicantId()).isEqualTo(7L);
        assertThat(application.getTargetCredit()).isEqualByComparingTo("4.00");
        assertThat(application.getStatus()).isEqualTo(Constants.STATUS_PENDING);
        assertThat(application.getFreezeFlowId()).isEqualTo(33L);
        verify(applicationMapper).insert(any(ConversionApplication.class));
        verify(applicationMapper).updateById(application);
    }

    @Test
    void approveDeductsFrozenCreditEarnsTargetCreditAndCreatesTransaction() {
        ConversionApplication application = pendingApplication(44L);
        LearnerOutcome source = outcome(11L, 7L, 100L, "10.00");
        CreditFlow deduct = flow(101L);
        CreditFlow earn = flow(102L);
        when(applicationMapper.selectById(44L)).thenReturn(application);
        when(applicationMapper.update(any(), any())).thenReturn(1);
        when(outcomeMapper.selectById(11L)).thenReturn(source);
        when(creditService.deductFrozen(eq(source), eq(new BigDecimal("5.00")), eq("conversion_application"), eq(44L), any()))
                .thenReturn(deduct);
        when(creditService.earn(eq(7L), any(), eq(new BigDecimal("4.00")), eq("conversion_application"), eq(44L), any()))
                .thenReturn(earn);

        service.approve(44L);

        assertThat(application.getStatus()).isEqualTo(Constants.STATUS_APPROVED);
        verify(outcomeMapper).insert(any(LearnerOutcome.class));
        ArgumentCaptor<ConversionTransaction> transactionCaptor = ArgumentCaptor.forClass(ConversionTransaction.class);
        verify(transactionMapper).insert(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getDeductFlowId()).isEqualTo(101L);
        assertThat(transactionCaptor.getValue().getEarnFlowId()).isEqualTo(102L);
    }

    @Test
    void rejectUnfreezesSourceCredit() {
        ConversionApplication application = pendingApplication(44L);
        LearnerOutcome source = outcome(11L, 7L, 100L, "10.00");
        when(applicationMapper.selectById(44L)).thenReturn(application);
        when(applicationMapper.update(any(), any())).thenReturn(1);
        when(outcomeMapper.selectById(11L)).thenReturn(source);

        service.reject(44L, "not eligible");

        assertThat(application.getStatus()).isEqualTo(Constants.STATUS_REJECTED);
        assertThat(application.getRejectReason()).isEqualTo("not eligible");
        verify(creditService).unfreeze(eq(source), eq(new BigDecimal("5.00")),
                eq("conversion_application"), eq(44L), any());
    }

    @Test
    void previewRejectsNonPositiveCredit() {
        assertThatThrownBy(() -> service.preview(22L, 11L, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("大于");
    }

    @Test
    void concurrentApprovalClaimFailureHasNoSideEffects() {
        when(applicationMapper.selectById(44L)).thenReturn(pendingApplication(44L));
        when(applicationMapper.update(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.approve(44L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("其他审核员");

        verify(creditService, never()).deductFrozen(any(), any(), any(), any(), any());
        verify(transactionMapper, never()).insert(any());
    }

    private LearnerOutcome outcome(Long id, Long userId, Long catalogId, String available) {
        LearnerOutcome outcome = new LearnerOutcome();
        outcome.setId(id);
        outcome.setUserId(userId);
        outcome.setCatalogId(catalogId);
        outcome.setAvailableCredit(new BigDecimal(available));
        outcome.setFrozenCredit(BigDecimal.ZERO);
        outcome.setTotalCredit(new BigDecimal(available));
        outcome.setStatus(Constants.STATUS_VALID);
        return outcome;
    }

    private ConversionRule rule(Long id, Long sourceCatalogId, Long targetCatalogId, String ratio) {
        ConversionRule rule = new ConversionRule();
        rule.setId(id);
        rule.setSourceCatalogId(sourceCatalogId);
        rule.setTargetCatalogId(targetCatalogId);
        rule.setConversionRatio(new BigDecimal(ratio));
        rule.setStatus(Constants.STATUS_EFFECTIVE);
        rule.setEnabled(1);
        return rule;
    }

    private ConversionApplication pendingApplication(Long id) {
        ConversionApplication application = new ConversionApplication();
        application.setId(id);
        application.setApplicantId(7L);
        application.setSourceOutcomeId(11L);
        application.setSourceCatalogId(100L);
        application.setTargetCatalogId(200L);
        application.setSourceCredit(new BigDecimal("5.00"));
        application.setConversionRatio(new BigDecimal("0.80"));
        application.setTargetCredit(new BigDecimal("4.00"));
        application.setStatus(Constants.STATUS_PENDING);
        return application;
    }

    private CreditFlow flow(Long id) {
        CreditFlow flow = new CreditFlow();
        flow.setId(id);
        return flow;
    }
}
