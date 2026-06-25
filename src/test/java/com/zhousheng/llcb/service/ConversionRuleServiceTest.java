package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.BizDtos;
import com.zhousheng.llcb.entity.ConversionRule;
import com.zhousheng.llcb.entity.ConversionRuleReview;
import com.zhousheng.llcb.entity.ExpertProfile;
import com.zhousheng.llcb.mapper.ConversionRuleMapper;
import com.zhousheng.llcb.mapper.ConversionRuleReviewMapper;
import com.zhousheng.llcb.mapper.ExpertProfileMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionRuleServiceTest {

    @Mock
    private ConversionRuleMapper ruleMapper;
    @Mock
    private ConversionRuleReviewMapper reviewMapper;
    @Mock
    private ExpertProfileMapper expertMapper;
    @Mock
    private AuditTrailService auditTrailService;
    @Mock
    private MessageService messageService;

    private ConversionRuleService service;

    @BeforeEach
    void setUp() {
        service = new ConversionRuleService(ruleMapper, reviewMapper, expertMapper, auditTrailService, messageService);
        TestSecurity.loginAs(9L, "qa_admin", "admin");
    }

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    @Test
    void submitReviewMovesDraftRuleToReviewing() {
        ConversionRule rule = rule(22L, "draft");
        when(ruleMapper.selectById(22L)).thenReturn(rule);

        service.submitReview(22L);

        assertThat(rule.getStatus()).isEqualTo(Constants.STATUS_REVIEWING);
        assertThat(rule.getSubmittedAt()).isNotNull();
        verify(ruleMapper).updateById(rule);
    }

    @Test
    void submitReviewRejectsNonDraftRule() {
        when(ruleMapper.selectById(22L)).thenReturn(rule(22L, Constants.STATUS_EFFECTIVE));

        assertThatThrownBy(() -> service.submitReview(22L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void assignExpertCreatesPendingReviewAndMessage() {
        ConversionRule rule = rule(22L, Constants.STATUS_REVIEWING);
        ExpertProfile expert = expert(33L, 77L, "available");
        when(ruleMapper.selectById(22L)).thenReturn(rule);
        when(expertMapper.selectById(33L)).thenReturn(expert);

        ConversionRuleReview review = service.assignExpert(22L, new BizDtos.ReviewAssignRequest(33L));

        assertThat(review.getRuleId()).isEqualTo(22L);
        assertThat(review.getExpertId()).isEqualTo(33L);
        assertThat(review.getStatus()).isEqualTo(Constants.STATUS_PENDING);
        verify(reviewMapper).insert(any(ConversionRuleReview.class));
        verify(messageService).send(eq(77L), any(), any(), eq("expert_review"),
                eq("rule_review_assigned"), eq("conversion_rule"), eq(22L));
    }

    @Test
    void expertSupportOpinionMakesRuleEffective() {
        TestSecurity.loginAs(77L, "qa_expert", "expert");
        ConversionRuleReview review = new ConversionRuleReview();
        review.setId(55L);
        review.setRuleId(22L);
        review.setExpertId(33L);
        review.setStatus(Constants.STATUS_PENDING);
        ExpertProfile expert = expert(33L, 77L, "available");
        ConversionRule rule = rule(22L, Constants.STATUS_REVIEWING);
        when(reviewMapper.selectById(55L)).thenReturn(review);
        when(expertMapper.selectById(33L)).thenReturn(expert);
        when(ruleMapper.selectById(22L)).thenReturn(rule);

        service.review(55L, new BizDtos.ExpertReviewRequest("support", "ok"));

        assertThat(review.getStatus()).isEqualTo("reviewed");
        assertThat(rule.getStatus()).isEqualTo(Constants.STATUS_EFFECTIVE);
        assertThat(rule.getEnabled()).isEqualTo(1);
        ArgumentCaptor<ConversionRuleReview> reviewCaptor = ArgumentCaptor.forClass(ConversionRuleReview.class);
        verify(reviewMapper).updateById(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().getOpinion()).isEqualTo("support");
    }

    private ConversionRule rule(Long id, String status) {
        ConversionRule rule = new ConversionRule();
        rule.setId(id);
        rule.setRuleName("Rule");
        rule.setStatus(status);
        rule.setEnabled(0);
        return rule;
    }

    private ExpertProfile expert(Long id, Long userId, String status) {
        ExpertProfile expert = new ExpertProfile();
        expert.setId(id);
        expert.setUserId(userId);
        expert.setExpertName("Expert");
        expert.setStatus(status);
        return expert;
    }
}
