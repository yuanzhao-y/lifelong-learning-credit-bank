package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("learner_outcome")
@EqualsAndHashCode(callSuper = true)
public class LearnerOutcome extends BaseEntity {
    private Long userId;
    private Long catalogId;
    private String sourceType;
    private Long sourceApplicationId;
    private String outcomeName;
    private String certificateNo;
    private String issuingAuthority;
    private LocalDate obtainedAt;
    private BigDecimal totalCredit;
    private BigDecimal availableCredit;
    private BigDecimal frozenCredit;
    private String status;
    private LocalDateTime certifiedAt;
}
