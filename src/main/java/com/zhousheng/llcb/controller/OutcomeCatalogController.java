package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.mapper.LearnOutcomeCatalogMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class OutcomeCatalogController {

    private final LearnOutcomeCatalogMapper catalogMapper;

    public OutcomeCatalogController(LearnOutcomeCatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    @GetMapping("/public/outcomes")
    public ApiResponse<Page<LearnOutcomeCatalog>> publicList(@RequestParam(defaultValue = "1") long page,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(required = false) String outcomeType,
                                                             @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(catalogMapper.selectPage(MybatisPlusConfig.page(page, size), baseQuery(outcomeType, "enabled", keyword)));
    }

    @GetMapping("/public/outcomes/{id}")
    public ApiResponse<LearnOutcomeCatalog> publicDetail(@PathVariable Long id) {
        return ApiResponse.ok(catalogMapper.selectById(id));
    }

    @GetMapping("/outcomes")
    public ApiResponse<Page<LearnOutcomeCatalog>> list(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "10") long size,
                                                       @RequestParam(required = false) String outcomeType,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(catalogMapper.selectPage(MybatisPlusConfig.page(page, size), baseQuery(outcomeType, status, keyword)));
    }

    @PostMapping("/outcomes")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<LearnOutcomeCatalog> create(@RequestBody LearnOutcomeCatalog catalog) {
        catalogMapper.insert(catalog);
        return ApiResponse.ok(catalog);
    }

    @PutMapping("/outcomes/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody LearnOutcomeCatalog catalog) {
        catalog.setId(id);
        catalogMapper.updateById(catalog);
        return ApiResponse.ok();
    }

    @DeleteMapping("/outcomes/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/outcomes/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        LearnOutcomeCatalog catalog = catalogMapper.selectById(id);
        catalog.setStatus(status);
        catalogMapper.updateById(catalog);
        return ApiResponse.ok();
    }

    @GetMapping("/outcomes/export")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<byte[]> exportCsv() {
        List<LearnOutcomeCatalog> catalogs = catalogMapper.selectList(new LambdaQueryWrapper<LearnOutcomeCatalog>()
                .orderByAsc(LearnOutcomeCatalog::getOutcomeCode));
        StringBuilder builder = new StringBuilder("outcome_code,outcome_name,outcome_type,base_credit,status\n");
        for (LearnOutcomeCatalog catalog : catalogs) {
            builder.append(csv(catalog.getOutcomeCode())).append(',')
                    .append(csv(catalog.getOutcomeName())).append(',')
                    .append(csv(catalog.getOutcomeType())).append(',')
                    .append(catalog.getBaseCredit()).append(',')
                    .append(csv(catalog.getStatus())).append('\n');
        }
        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outcome_catalog.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    private LambdaQueryWrapper<LearnOutcomeCatalog> baseQuery(String outcomeType, String status, String keyword) {
        return new LambdaQueryWrapper<LearnOutcomeCatalog>()
                .eq(StringUtils.hasText(outcomeType), LearnOutcomeCatalog::getOutcomeType, outcomeType)
                .eq(StringUtils.hasText(status), LearnOutcomeCatalog::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(LearnOutcomeCatalog::getOutcomeName, keyword)
                        .or()
                        .like(LearnOutcomeCatalog::getOutcomeCode, keyword))
                .orderByDesc(LearnOutcomeCatalog::getCreatedAt);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
