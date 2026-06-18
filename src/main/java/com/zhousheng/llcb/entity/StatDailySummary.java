package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("stat_daily_summary")
@EqualsAndHashCode(callSuper = true)
public class StatDailySummary extends BaseEntity {
    private LocalDate statDate;
    private Long userTotal;
    private Long userNewCount;
    private Long outcomeTotal;
    private Long certApplyCount;
    private Long certPassCount;
    private Long conversionApplyCount;
    private Long conversionSuccessCount;
    private BigDecimal creditIssuedTotal;
}
