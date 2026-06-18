package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("conversion_rule")
@EqualsAndHashCode(callSuper = true)
public class ConversionRule extends BaseEntity {
    private String ruleCode;
    private String ruleName;
    private Long sourceCatalogId;
    private Long targetCatalogId;
    private BigDecimal conversionRatio;
    private String status;
    private Integer enabled;
    private String description;
    private String applicableCondition;
    private LocalDateTime submittedAt;
    private LocalDateTime effectiveAt;
    private LocalDateTime abolishedAt;
}
