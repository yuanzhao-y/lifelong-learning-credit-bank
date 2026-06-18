package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cert_application")
@EqualsAndHashCode(callSuper = true)
public class CertApplication extends BaseEntity {
    private String applicationNo;
    private Long applicantId;
    private Long catalogId;
    private String certifyType;
    private String outcomeName;
    private String certificateNo;
    private String issuingAuthority;
    private LocalDate obtainedAt;
    private BigDecimal requestedCredit;
    private BigDecimal recognizedCredit;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime withdrawnAt;
    private Long auditUserId;
    private LocalDateTime auditedAt;
    private String rejectReason;
}
