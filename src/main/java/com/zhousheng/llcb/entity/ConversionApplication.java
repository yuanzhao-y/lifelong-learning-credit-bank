package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("conversion_application")
@EqualsAndHashCode(callSuper = true)
public class ConversionApplication extends BaseEntity {
    private String applicationNo;
    private Long applicantId;
    private Long ruleId;
    private Long sourceOutcomeId;
    private Long sourceCatalogId;
    private Long targetCatalogId;
    private BigDecimal sourceCredit;
    private BigDecimal conversionRatio;
    private BigDecimal targetCredit;
    private String status;
    private Long freezeFlowId;
    private Long targetOutcomeId;
    private LocalDateTime submittedAt;
    private Long auditUserId;
    private LocalDateTime auditedAt;
    private String rejectReason;
}
