package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.mapper.LearnOutcomeCatalogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutcomeCatalogControllerTest {

    @Mock
    private LearnOutcomeCatalogMapper catalogMapper;

    private OutcomeCatalogController controller;

    @BeforeEach
    void setUp() {
        controller = new OutcomeCatalogController(catalogMapper);
    }

    @Test
    void importCsvCreatesUpdatesAndReportsInvalidRows() {
        LearnOutcomeCatalog existing = new LearnOutcomeCatalog();
        existing.setId(88L);
        when(catalogMapper.selectOne(any())).thenReturn(null, existing);
        MockMultipartFile file = csv("""
                outcome_code,outcome_name,outcome_type,base_credit,status
                QA-NEW,导入新增成果,course_cert,3.50,enabled
                QA-OLD,导入更新成果,vocational_qualification,8.00,disabled
                QA-BAD,坏数据,course_cert,abc,enabled
                """);

        OutcomeCatalogController.ImportResult result = controller.importCsv(file).getData();

        assertThat(result.totalRows()).isEqualTo(3);
        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(result.errors()).singleElement()
                .satisfies(error -> {
                    assertThat(error.row()).isEqualTo(4);
                    assertThat(error.message()).contains("标准学分格式错误");
                });
        ArgumentCaptor<LearnOutcomeCatalog> insertCaptor = ArgumentCaptor.forClass(LearnOutcomeCatalog.class);
        verify(catalogMapper).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().getOutcomeCode()).isEqualTo("QA-NEW");
        assertThat(insertCaptor.getValue().getBaseCredit()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(insertCaptor.getValue().getAuditStatus()).isEqualTo(Constants.STATUS_APPROVED);
        ArgumentCaptor<LearnOutcomeCatalog> updateCaptor = ArgumentCaptor.forClass(LearnOutcomeCatalog.class);
        verify(catalogMapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getId()).isEqualTo(88L);
        assertThat(updateCaptor.getValue().getStatus()).isEqualTo(Constants.STATUS_DISABLED);
    }

    @Test
    void importCsvRejectsMissingRequiredHeader() {
        MockMultipartFile file = csv("""
                code,name,type,credit,status
                QA-01,缺表头,course_cert,1.0,enabled
                """);

        assertThatThrownBy(() -> controller.importCsv(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CSV 表头必须包含");
    }

    private MockMultipartFile csv(String content) {
        return new MockMultipartFile("file", "outcomes.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }
}
