package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("credit_account")
@EqualsAndHashCode(callSuper = true)
public class CreditAccount extends BaseEntity {
    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenCredit;
    private BigDecimal totalEarned;
    private BigDecimal totalDeducted;
    @Version
    private Integer version;
}
