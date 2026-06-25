package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.entity.SysOperationLog;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSystemControllerTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysApiPermissionMapper apiPermissionMapper;
    @Mock
    private SysDictMapper dictMapper;
    @Mock
    private SysDictItemMapper dictItemMapper;
    @Mock
    private SysOperationLogMapper operationLogMapper;
    @Mock
    private RoleService roleService;

    private AdminSystemController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSystemController(roleMapper, menuMapper, apiPermissionMapper, dictMapper,
                dictItemMapper, operationLogMapper, roleService);
    }

    @Test
    void exportLogsReturnsCsvWithBomAndEscapedFields() {
        SysOperationLog log = new SysOperationLog();
        log.setOperatorId(7L);
        log.setOperatorName("qa_admin");
        log.setModule("OutcomeCatalogController");
        log.setOperationType("importCsv");
        log.setOperationContent("导入 \"CSV\" 成果目录");
        log.setIpAddress("127.0.0.1");
        log.setOperatedAt(LocalDateTime.of(2026, 6, 23, 9, 30, 0));
        when(operationLogMapper.selectList(any())).thenReturn(List.of(log));

        ResponseEntity<byte[]> response = controller.exportLogs(7L, "importCsv",
                LocalDateTime.of(2026, 6, 23, 0, 0, 0),
                LocalDateTime.of(2026, 6, 23, 23, 59, 59));

        assertThat(response.getHeaders().getContentDisposition().toString())
                .contains("operation-logs-")
                .contains(".csv");
        String csv = new String(response.getBody(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFFoperator_id,operator_name,module,operation_type");
        assertThat(csv).contains("\"7\",\"qa_admin\",\"OutcomeCatalogController\",\"importCsv\"");
        assertThat(csv).contains("\"导入 \"\"CSV\"\" 成果目录\"");
        verify(operationLogMapper).selectList(any());
    }
}
