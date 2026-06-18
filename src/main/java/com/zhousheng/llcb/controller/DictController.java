package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.entity.SysDictItem;
import com.zhousheng.llcb.service.DictCacheService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dicts")
public class DictController {

    private final DictCacheService dictCacheService;

    public DictController(DictCacheService dictCacheService) {
        this.dictCacheService = dictCacheService;
    }

    @GetMapping("/{dictCode}/items")
    public ApiResponse<List<SysDictItem>> items(@PathVariable String dictCode) {
        return ApiResponse.ok(dictCacheService.items(dictCode));
    }

    @DeleteMapping("/{dictCode}/cache")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> evict(@PathVariable String dictCode) {
        dictCacheService.evict(dictCode);
        return ApiResponse.ok();
    }
}
