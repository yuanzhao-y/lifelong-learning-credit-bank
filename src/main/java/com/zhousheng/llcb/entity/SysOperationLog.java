package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
@EqualsAndHashCode(callSuper = true)
public class SysOperationLog extends BaseEntity {
    private Long operatorId;
    private String operatorName;
    private String module;
    private String operationType;
    private String operationContent;
    private String requestMethod;
    private String requestUri;
    private String ipAddress;
    private String userAgent;
    private String result;
    private String errorMessage;
    private LocalDateTime operatedAt;
}
