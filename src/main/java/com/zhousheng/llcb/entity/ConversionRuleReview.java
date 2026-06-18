package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("conversion_rule_review")
@EqualsAndHashCode(callSuper = true)
public class ConversionRuleReview extends BaseEntity {
    private Long ruleId;
    private Long expertId;
    private Long assignedBy;
    private LocalDateTime assignedAt;
    private String status;
    private String opinion;
    private String reviewComment;
    private LocalDateTime reviewedAt;
}
