package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.entity.CreditAccount;
import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.mapper.CreditFlowMapper;
import com.zhousheng.llcb.service.CreditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditControllerTest {

    @Mock
    private CreditService creditService;
    @Mock
    private CreditFlowMapper creditFlowMapper;

    private CreditController controller;

    @BeforeEach
    void setUp() {
        controller = new CreditController(creditService, creditFlowMapper);
        TestSecurity.loginAs(88L, "qa_learner", "learner");
    }

    @AfterEach
    void clear() {
        TestSecurity.clear();
    }

    @Test
    void accountUsesCurrentUser() {
        CreditAccount account = new CreditAccount();
        account.setUserId(88L);
        when(creditService.getOrCreateAccount(88L)).thenReturn(account);

        assertThat(controller.account().getData().getUserId()).isEqualTo(88L);
    }

    @Test
    void flowsAcceptsAllOptionalFilters() {
        Page<CreditFlow> page = new Page<>(2, 20);
        when(creditFlowMapper.selectPage(any(), any())).thenReturn(page);

        assertThat(controller.flows(2, 20, "certification",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 25)).getData()).isSameAs(page);
        verify(creditFlowMapper).selectPage(any(), any());
    }

    @Test
    void flowsAllowsEmptyFilters() {
        Page<CreditFlow> page = new Page<>(1, 10);
        when(creditFlowMapper.selectPage(any(), any())).thenReturn(page);

        assertThat(controller.flows(1, 10, "  ", null, null).getData()).isSameAs(page);
    }
}
