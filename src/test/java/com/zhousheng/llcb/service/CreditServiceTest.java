package com.zhousheng.llcb.service;

import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.entity.CreditAccount;
import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.mapper.CreditAccountMapper;
import com.zhousheng.llcb.mapper.CreditFlowMapper;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditAccountMapper accountMapper;
    @Mock
    private CreditFlowMapper flowMapper;
    @Mock
    private LearnerOutcomeMapper outcomeMapper;

    private CreditService service;

    @BeforeEach
    void setUp() {
        service = new CreditService(accountMapper, flowMapper, outcomeMapper);
    }

    @Test
    void getOrCreateAccountReturnsExistingAccount() {
        CreditAccount account = account(1L, 7L, "10.00", "0.00");
        when(accountMapper.selectOne(any())).thenReturn(account);

        CreditAccount result = service.getOrCreateAccount(7L);

        assertThat(result).isSameAs(account);
        verify(accountMapper, never()).insert(any());
    }

    @Test
    void earnUpdatesBalanceAndCreatesFlow() {
        CreditAccount account = account(1L, 7L, "10.00", "0.00");
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(1);

        CreditFlow flow = service.earn(7L, 11L, bd("5.00"), "cert_application", 99L, "earned");

        assertThat(account.getBalance()).isEqualByComparingTo("15.00");
        assertThat(account.getTotalEarned()).isEqualByComparingTo("5.00");
        assertThat(flow.getChangeType()).isEqualTo("earn");
        assertThat(flow.getBalanceBefore()).isEqualByComparingTo("10.00");
        assertThat(flow.getBalanceAfter()).isEqualByComparingTo("15.00");
        verify(flowMapper).insert(any(CreditFlow.class));
    }

    @Test
    void freezeRejectsWhenOutcomeAvailableCreditIsInsufficient() {
        LearnerOutcome outcome = outcome(11L, 7L, "3.00", "0.00");

        assertThatThrownBy(() -> service.freeze(outcome, bd("5.00"), "conversion_application", 1L, "freeze"))
                .isInstanceOf(BusinessException.class);

        verify(accountMapper, never()).updateById(any());
        verify(flowMapper, never()).insert(any());
    }

    @Test
    void freezeMovesBalanceToFrozenCredit() {
        LearnerOutcome outcome = outcome(11L, 7L, "10.00", "0.00");
        CreditAccount account = account(1L, 7L, "10.00", "0.00");
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(1);
        when(outcomeMapper.update(any(), any())).thenReturn(1);

        service.freeze(outcome, bd("4.00"), "conversion_application", 1L, "freeze");

        assertThat(account.getBalance()).isEqualByComparingTo("6.00");
        assertThat(account.getFrozenCredit()).isEqualByComparingTo("4.00");
        assertThat(outcome.getAvailableCredit()).isEqualByComparingTo("6.00");
        assertThat(outcome.getFrozenCredit()).isEqualByComparingTo("4.00");
        verify(outcomeMapper).update(any(), any());
    }

    @Test
    void deductFrozenUpdatesTotalsAndCreatesDeductFlow() {
        LearnerOutcome outcome = outcome(11L, 7L, "6.00", "4.00");
        outcome.setTotalCredit(bd("10.00"));
        CreditAccount account = account(1L, 7L, "6.00", "4.00");
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(1);
        when(outcomeMapper.update(any(), any())).thenReturn(1);

        service.deductFrozen(outcome, bd("4.00"), "conversion_application", 1L, "deduct");

        assertThat(account.getFrozenCredit()).isEqualByComparingTo("0.00");
        assertThat(account.getTotalDeducted()).isEqualByComparingTo("4.00");
        assertThat(outcome.getTotalCredit()).isEqualByComparingTo("6.00");
        ArgumentCaptor<CreditFlow> captor = ArgumentCaptor.forClass(CreditFlow.class);
        verify(flowMapper).insert(captor.capture());
        assertThat(captor.getValue().getChangeType()).isEqualTo("deduct");
    }

    @Test
    void throwsWhenOptimisticLockUpdateFails() {
        CreditAccount account = account(1L, 7L, "10.00", "0.00");
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(0);

        assertThatThrownBy(() -> service.earn(7L, 11L, bd("1.00"), "cert_application", 99L, "earned"))
                .isInstanceOf(BusinessException.class);

        verify(flowMapper, never()).insert(any());
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> service.earn(7L, 11L, BigDecimal.ZERO,
                "cert_application", 99L, "earned"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("大于");

        verify(accountMapper, never()).updateById(any());
        verify(flowMapper, never()).insert(any());
    }

    @Test
    void outcomeConcurrentUpdateFailureDoesNotCreateFlow() {
        LearnerOutcome outcome = outcome(11L, 7L, "10.00", "0.00");
        CreditAccount account = account(1L, 7L, "10.00", "0.00");
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(1);
        when(outcomeMapper.update(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.freeze(outcome, bd("4.00"),
                "conversion_application", 1L, "freeze"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("并发");

        verify(flowMapper, never()).insert(any());
    }

    private CreditAccount account(Long id, Long userId, String balance, String frozen) {
        CreditAccount account = new CreditAccount();
        account.setId(id);
        account.setUserId(userId);
        account.setBalance(bd(balance));
        account.setFrozenCredit(bd(frozen));
        account.setTotalEarned(BigDecimal.ZERO);
        account.setTotalDeducted(BigDecimal.ZERO);
        account.setVersion(0);
        return account;
    }

    private LearnerOutcome outcome(Long id, Long userId, String available, String frozen) {
        LearnerOutcome outcome = new LearnerOutcome();
        outcome.setId(id);
        outcome.setUserId(userId);
        outcome.setAvailableCredit(bd(available));
        outcome.setFrozenCredit(bd(frozen));
        outcome.setTotalCredit(bd(available).add(bd(frozen)));
        return outcome;
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
