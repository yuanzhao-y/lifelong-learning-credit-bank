package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("credit_flow")
@EqualsAndHashCode(callSuper = true)
public class CreditFlow extends BaseEntity {
    private String flowNo;
    private Long accountId;
    private Long userId;
    private Long learnerOutcomeId;
    private String bizType;
    private Long bizId;
    private String changeType;
    private BigDecimal creditAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal frozenBefore;
    private BigDecimal frozenAfter;
    private String description;
}
