package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("conversion_transaction")
@EqualsAndHashCode(callSuper = true)
public class ConversionTransaction extends BaseEntity {
    private String transactionNo;
    private Long applicationId;
    private Long userId;
    private Long sourceOutcomeId;
    private Long targetOutcomeId;
    private Long deductFlowId;
    private Long earnFlowId;
    private BigDecimal sourceCredit;
    private BigDecimal targetCredit;
    private LocalDateTime convertedAt;
}
