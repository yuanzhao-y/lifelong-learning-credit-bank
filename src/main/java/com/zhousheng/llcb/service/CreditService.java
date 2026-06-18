package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.entity.CreditAccount;
import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.mapper.CreditAccountMapper;
import com.zhousheng.llcb.mapper.CreditFlowMapper;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreditService {

    private final CreditAccountMapper accountMapper;
    private final CreditFlowMapper flowMapper;
    private final LearnerOutcomeMapper outcomeMapper;

    public CreditService(CreditAccountMapper accountMapper,
                         CreditFlowMapper flowMapper,
                         LearnerOutcomeMapper outcomeMapper) {
        this.accountMapper = accountMapper;
        this.flowMapper = flowMapper;
        this.outcomeMapper = outcomeMapper;
    }

    @Transactional
    public CreditAccount getOrCreateAccount(Long userId) {
        CreditAccount account = accountMapper.selectOne(new LambdaQueryWrapper<CreditAccount>()
                .eq(CreditAccount::getUserId, userId)
                .last("limit 1"));
        if (account != null) {
            return account;
        }
        account = new CreditAccount();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenCredit(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);
        account.setTotalDeducted(BigDecimal.ZERO);
        account.setVersion(0);
        accountMapper.insert(account);
        return account;
    }

    @Transactional
    public CreditFlow earn(Long userId, Long learnerOutcomeId, BigDecimal amount, String bizType, Long bizId, String description) {
        CreditAccount account = getOrCreateAccount(userId);
        BigDecimal balanceBefore = nvl(account.getBalance());
        BigDecimal frozenBefore = nvl(account.getFrozenCredit());
        account.setBalance(balanceBefore.add(amount));
        account.setTotalEarned(nvl(account.getTotalEarned()).add(amount));
        accountMapper.updateById(account);
        return flow(account, learnerOutcomeId, bizType, bizId, "earn", amount, balanceBefore, account.getBalance(), frozenBefore, frozenBefore, description);
    }

    @Transactional
    public CreditFlow freeze(LearnerOutcome outcome, BigDecimal amount, String bizType, Long bizId, String description) {
        if (nvl(outcome.getAvailableCredit()).compareTo(amount) < 0) {
            throw new BusinessException("可用学分不足");
        }
        CreditAccount account = getOrCreateAccount(outcome.getUserId());
        if (nvl(account.getBalance()).compareTo(amount) < 0) {
            throw new BusinessException("账户可用学分不足");
        }
        BigDecimal balanceBefore = nvl(account.getBalance());
        BigDecimal frozenBefore = nvl(account.getFrozenCredit());
        account.setBalance(balanceBefore.subtract(amount));
        account.setFrozenCredit(frozenBefore.add(amount));
        accountMapper.updateById(account);

        outcome.setAvailableCredit(nvl(outcome.getAvailableCredit()).subtract(amount));
        outcome.setFrozenCredit(nvl(outcome.getFrozenCredit()).add(amount));
        outcomeMapper.updateById(outcome);
        return flow(account, outcome.getId(), bizType, bizId, "freeze", amount, balanceBefore, account.getBalance(), frozenBefore, account.getFrozenCredit(), description);
    }

    @Transactional
    public CreditFlow unfreeze(LearnerOutcome outcome, BigDecimal amount, String bizType, Long bizId, String description) {
        CreditAccount account = getOrCreateAccount(outcome.getUserId());
        BigDecimal balanceBefore = nvl(account.getBalance());
        BigDecimal frozenBefore = nvl(account.getFrozenCredit());
        account.setBalance(balanceBefore.add(amount));
        account.setFrozenCredit(frozenBefore.subtract(amount));
        accountMapper.updateById(account);

        outcome.setAvailableCredit(nvl(outcome.getAvailableCredit()).add(amount));
        outcome.setFrozenCredit(nvl(outcome.getFrozenCredit()).subtract(amount));
        outcomeMapper.updateById(outcome);
        return flow(account, outcome.getId(), bizType, bizId, "unfreeze", amount, balanceBefore, account.getBalance(), frozenBefore, account.getFrozenCredit(), description);
    }

    @Transactional
    public CreditFlow deductFrozen(LearnerOutcome outcome, BigDecimal amount, String bizType, Long bizId, String description) {
        CreditAccount account = getOrCreateAccount(outcome.getUserId());
        if (nvl(account.getFrozenCredit()).compareTo(amount) < 0 || nvl(outcome.getFrozenCredit()).compareTo(amount) < 0) {
            throw new BusinessException("冻结学分不足");
        }
        BigDecimal balanceBefore = nvl(account.getBalance());
        BigDecimal frozenBefore = nvl(account.getFrozenCredit());
        account.setFrozenCredit(frozenBefore.subtract(amount));
        account.setTotalDeducted(nvl(account.getTotalDeducted()).add(amount));
        accountMapper.updateById(account);

        outcome.setFrozenCredit(nvl(outcome.getFrozenCredit()).subtract(amount));
        outcome.setTotalCredit(nvl(outcome.getTotalCredit()).subtract(amount));
        outcomeMapper.updateById(outcome);
        return flow(account, outcome.getId(), bizType, bizId, "deduct", amount, balanceBefore, balanceBefore, frozenBefore, account.getFrozenCredit(), description);
    }

    private CreditFlow flow(CreditAccount account,
                            Long learnerOutcomeId,
                            String bizType,
                            Long bizId,
                            String changeType,
                            BigDecimal amount,
                            BigDecimal balanceBefore,
                            BigDecimal balanceAfter,
                            BigDecimal frozenBefore,
                            BigDecimal frozenAfter,
                            String description) {
        CreditFlow flow = new CreditFlow();
        flow.setFlowNo(BizNoGenerator.next("CF"));
        flow.setAccountId(account.getId());
        flow.setUserId(account.getUserId());
        flow.setLearnerOutcomeId(learnerOutcomeId);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setChangeType(changeType);
        flow.setCreditAmount(amount);
        flow.setBalanceBefore(balanceBefore);
        flow.setBalanceAfter(balanceAfter);
        flow.setFrozenBefore(frozenBefore);
        flow.setFrozenAfter(frozenAfter);
        flow.setDescription(description);
        flowMapper.insert(flow);
        return flow;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
