package com.zhousheng.llcb.service;

import com.zhousheng.llcb.entity.BizAuditRecord;
import com.zhousheng.llcb.mapper.BizAuditRecordMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditTrailService {

    private final BizAuditRecordMapper auditRecordMapper;

    public AuditTrailService(BizAuditRecordMapper auditRecordMapper) {
        this.auditRecordMapper = auditRecordMapper;
    }

    public void record(String bizType, Long bizId, String action, String fromStatus, String toStatus, String opinion) {
        BizAuditRecord record = new BizAuditRecord();
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setOperatorId(SecurityUtils.currentUserIdOrNull());
        record.setAction(action);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setOpinion(opinion);
        record.setOperatedAt(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }
}
