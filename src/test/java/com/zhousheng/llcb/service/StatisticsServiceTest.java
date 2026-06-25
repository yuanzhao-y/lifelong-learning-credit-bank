package com.zhousheng.llcb.service;

import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private SysUserMapper userMapper;
    @Mock private LearnOutcomeCatalogMapper catalogMapper;
    @Mock private CertApplicationMapper certMapper;
    @Mock private ConversionApplicationMapper conversionMapper;
    @Mock private CreditFlowMapper flowMapper;

    private StatisticsService service;

    @BeforeEach
    void setUp() {
        service = new StatisticsService(userMapper, catalogMapper, certMapper, conversionMapper, flowMapper);
    }

    @Test
    void overviewAggregatesCountsCreditsAndOutcomeTypes() {
        when(userMapper.selectCount(null)).thenReturn(5L);
        when(catalogMapper.selectCount(null)).thenReturn(2L);
        when(certMapper.selectCount(any())).thenReturn(4L, 3L);
        when(conversionMapper.selectCount(any())).thenReturn(2L, 1L);
        CreditFlow first = flow("2.50");
        CreditFlow second = flow("7.50");
        when(flowMapper.selectList(any())).thenReturn(List.of(first, second));
        LearnOutcomeCatalog course = catalog("course");
        LearnOutcomeCatalog skill = catalog("skill");
        when(catalogMapper.selectList(null)).thenReturn(List.of(course, course, skill));

        Map<String, Object> result = service.overview(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(result.get("userTotal")).isEqualTo(5L);
        assertThat(result.get("creditIssuedTotal")).isEqualTo(new BigDecimal("10.00"));
        @SuppressWarnings("unchecked")
        Map<String, Long> distribution = (Map<String, Long>) result.get("outcomeTypeDistribution");
        assertThat(distribution)
                .containsEntry("course", 2L)
                .containsEntry("skill", 1L);
    }

    @Test
    void overviewSupportsOpenDateRange() {
        when(userMapper.selectCount(null)).thenReturn(0L);
        when(catalogMapper.selectCount(null)).thenReturn(0L);
        when(certMapper.selectCount(any())).thenReturn(0L);
        when(conversionMapper.selectCount(any())).thenReturn(0L);
        when(flowMapper.selectList(any())).thenReturn(List.of());
        when(catalogMapper.selectList(null)).thenReturn(List.of());

        assertThat(service.overview(null, null).get("creditIssuedTotal")).isEqualTo(BigDecimal.ZERO);
    }

    private CreditFlow flow(String amount) {
        CreditFlow flow = new CreditFlow();
        flow.setCreditAmount(new BigDecimal(amount));
        return flow;
    }

    private LearnOutcomeCatalog catalog(String type) {
        LearnOutcomeCatalog catalog = new LearnOutcomeCatalog();
        catalog.setOutcomeType(type);
        return catalog;
    }
}
