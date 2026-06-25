package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.entity.BizAuditRecord;
import com.zhousheng.llcb.mapper.BizAuditRecordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceTest {

    @Mock
    private BizAuditRecordMapper mapper;

    @AfterEach
    void clear() {
        TestSecurity.clear();
    }

    @Test
    void recordsOperatorAndStateTransition() {
        TestSecurity.loginAs(3L, "qa_auditor", "auditor");
        AuditTrailService service = new AuditTrailService(mapper);

        service.record("cert_application", 8L, "approve", "pending", "approved", "ok");

        ArgumentCaptor<BizAuditRecord> captor = ArgumentCaptor.forClass(BizAuditRecord.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getOperatorId()).isEqualTo(3L);
        assertThat(captor.getValue().getFromStatus()).isEqualTo("pending");
        assertThat(captor.getValue().getToStatus()).isEqualTo("approved");
    }
}
