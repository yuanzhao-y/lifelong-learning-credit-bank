package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("learn_outcome_catalog")
@EqualsAndHashCode(callSuper = true)
public class LearnOutcomeCatalog extends BaseEntity {
    private String outcomeCode;
    private String outcomeName;
    private String outcomeType;
    private BigDecimal baseCredit;
    private String auditStatus;
    private String status;
    private String intro;
    private String certificationRequirements;
    private String applicableScope;
}
