package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final SysUserMapper userMapper;
    private final LearnOutcomeCatalogMapper catalogMapper;
    private final CertApplicationMapper certApplicationMapper;
    private final ConversionApplicationMapper conversionApplicationMapper;
    private final CreditFlowMapper creditFlowMapper;

    public StatisticsService(SysUserMapper userMapper,
                             LearnOutcomeCatalogMapper catalogMapper,
                             CertApplicationMapper certApplicationMapper,
                             ConversionApplicationMapper conversionApplicationMapper,
                             CreditFlowMapper creditFlowMapper) {
        this.userMapper = userMapper;
        this.catalogMapper = catalogMapper;
        this.certApplicationMapper = certApplicationMapper;
        this.conversionApplicationMapper = conversionApplicationMapper;
        this.creditFlowMapper = creditFlowMapper;
    }

    public Map<String, Object> overview(LocalDate start, LocalDate end) {
        LocalDateTime begin = start == null ? null : LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime finish = end == null ? null : LocalDateTime.of(end, LocalTime.MAX);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userTotal", userMapper.selectCount(null));
        result.put("outcomeTotal", catalogMapper.selectCount(null));
        result.put("certApplicationTotal", certApplicationMapper.selectCount(timeWrapper(new QueryWrapper<CertApplication>(), begin, finish)));
        result.put("certApplicationPassed", certApplicationMapper.selectCount(timeWrapper(new QueryWrapper<CertApplication>()
                .eq("status", Constants.STATUS_APPROVED), begin, finish)));
        result.put("conversionApplicationTotal", conversionApplicationMapper.selectCount(timeWrapper(new QueryWrapper<ConversionApplication>(), begin, finish)));
        result.put("conversionApplicationPassed", conversionApplicationMapper.selectCount(timeWrapper(new QueryWrapper<ConversionApplication>()
                .eq("status", Constants.STATUS_APPROVED), begin, finish)));
        BigDecimal issued = creditFlowMapper.selectList(timeWrapper(new QueryWrapper<CreditFlow>()
                        .eq("change_type", "earn"), begin, finish))
                .stream()
                .map(CreditFlow::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("creditIssuedTotal", issued);
        result.put("outcomeTypeDistribution", catalogMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(LearnOutcomeCatalog::getOutcomeType, Collectors.counting())));
        return result;
    }

    private <T> QueryWrapper<T> timeWrapper(QueryWrapper<T> wrapper, LocalDateTime begin, LocalDateTime finish) {
        if (begin != null) {
            wrapper.ge("created_at", begin);
        }
        if (finish != null) {
            wrapper.le("created_at", finish);
        }
        return wrapper;
    }
}
