package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.LearnerOutcome;
import com.zhousheng.llcb.mapper.LearnerOutcomeMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/learner-outcomes")
public class LearnerOutcomeController {

    private final LearnerOutcomeMapper outcomeMapper;

    public LearnerOutcomeController(LearnerOutcomeMapper outcomeMapper) {
        this.outcomeMapper = outcomeMapper;
    }

    @GetMapping("/mine")
    public ApiResponse<Page<LearnerOutcome>> mine(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(outcomeMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<LearnerOutcome>()
                        .eq(LearnerOutcome::getUserId, SecurityUtils.currentUserId())
                        .orderByDesc(LearnerOutcome::getCertifiedAt)));
    }
}
