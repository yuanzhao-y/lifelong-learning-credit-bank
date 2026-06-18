package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_audit_record")
@EqualsAndHashCode(callSuper = true)
public class BizAuditRecord extends BaseEntity {
    private String bizType;
    private Long bizId;
    private Long operatorId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String opinion;
    private LocalDateTime operatedAt;
}
