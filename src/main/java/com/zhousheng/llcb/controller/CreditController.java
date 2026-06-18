package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.CreditAccount;
import com.zhousheng.llcb.entity.CreditFlow;
import com.zhousheng.llcb.mapper.CreditFlowMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.CreditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/credits")
public class CreditController {

    private final CreditService creditService;
    private final CreditFlowMapper creditFlowMapper;

    public CreditController(CreditService creditService, CreditFlowMapper creditFlowMapper) {
        this.creditService = creditService;
        this.creditFlowMapper = creditFlowMapper;
    }

    @GetMapping("/account")
    public ApiResponse<CreditAccount> account() {
        return ApiResponse.ok(creditService.getOrCreateAccount(SecurityUtils.currentUserId()));
    }

    @GetMapping("/flows")
    public ApiResponse<Page<CreditFlow>> flows(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String changeType,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        LocalDateTime begin = start == null ? null : LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime finish = end == null ? null : LocalDateTime.of(end, LocalTime.MAX);
        return ApiResponse.ok(creditFlowMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<CreditFlow>()
                        .eq(CreditFlow::getUserId, SecurityUtils.currentUserId())
                        .eq(StringUtils.hasText(changeType), CreditFlow::getChangeType, changeType)
                        .ge(begin != null, CreditFlow::getCreatedAt, begin)
                        .le(finish != null, CreditFlow::getCreatedAt, finish)
                        .orderByDesc(CreditFlow::getCreatedAt)));
    }
}
