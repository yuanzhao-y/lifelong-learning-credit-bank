package com.zhousheng.llcb.controller;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.common.CsvSecurity;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.LearnOutcomeCatalog;
import com.zhousheng.llcb.mapper.LearnOutcomeCatalogMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OutcomeCatalogController {

    private static final long MAX_IMPORT_BYTES = 5L * 1024 * 1024;
    private static final int MAX_IMPORT_ROWS = 10_000;

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
        LearnOutcomeCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null || !Constants.STATUS_ENABLED.equals(catalog.getStatus())) {
            throw new BusinessException(404, "成果目录不存在");
        }
        return ApiResponse.ok(catalog);
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
        if (catalog == null) {
            throw new BusinessException(404, "成果目录不存在");
        }
        if (!Constants.STATUS_ENABLED.equals(status) && !Constants.STATUS_DISABLED.equals(status)) {
            throw new BusinessException("状态只能为 enabled 或 disabled");
        }
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
            builder.append(CsvSecurity.cell(catalog.getOutcomeCode())).append(',')
                    .append(CsvSecurity.cell(catalog.getOutcomeName())).append(',')
                    .append(CsvSecurity.cell(catalog.getOutcomeType())).append(',')
                    .append(catalog.getBaseCredit()).append(',')
                    .append(CsvSecurity.cell(catalog.getStatus())).append('\n');
        }
        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outcome_catalog.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/outcomes/import")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ImportResult> importCsv(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("导入文件不能为空");
        }
        if (file.getSize() > MAX_IMPORT_BYTES) {
            throw new BusinessException("CSV 文件不能超过 5 MB");
        }
        CsvReadConfig config = CsvReadConfig.defaultConfig()
                .setContainsHeader(true)
                .setSkipEmptyRows(true)
                .setTrimField(true);
        CsvData data;
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            StringReader reader = new StringReader(content);
            data = CsvUtil.getReader(reader, config).read();
        } catch (IOException ex) {
            throw new BusinessException("读取导入文件失败");
        }
        validateImportHeader(data.getHeader());
        if (data.getRowCount() > MAX_IMPORT_ROWS) {
            throw new BusinessException("单次导入不能超过 10000 行");
        }

        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        List<ImportError> errors = new ArrayList<>();
        List<CsvRow> rows = data.getRows();
        for (int i = 0; i < rows.size(); i++) {
            CsvRow row = rows.get(i);
            int rowNumber = i + 2;
            try {
                LearnOutcomeCatalog catalog = parseImportRow(row);
                LearnOutcomeCatalog existing = catalogMapper.selectOne(new LambdaQueryWrapper<LearnOutcomeCatalog>()
                        .eq(LearnOutcomeCatalog::getOutcomeCode, catalog.getOutcomeCode())
                        .last("LIMIT 1"));
                if (existing == null) {
                    catalogMapper.insert(catalog);
                    createdCount++;
                } else {
                    catalog.setId(existing.getId());
                    catalogMapper.updateById(catalog);
                    updatedCount++;
                }
            } catch (RuntimeException ex) {
                skippedCount++;
                errors.add(new ImportError(rowNumber, ex.getMessage()));
            }
        }
        if (createdCount + updatedCount == 0) {
            throw new BusinessException("没有有效的成果目录数据");
        }
        return ApiResponse.ok(new ImportResult(rows.size(), createdCount, updatedCount, skippedCount, errors));
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

    private void validateImportHeader(List<String> header) {
        List<String> required = List.of("outcome_code", "outcome_name", "outcome_type", "base_credit", "status");
        if (header == null || !header.containsAll(required)) {
            throw new BusinessException("CSV 表头必须包含 outcome_code,outcome_name,outcome_type,base_credit,status");
        }
    }

    private LearnOutcomeCatalog parseImportRow(CsvRow row) {
        String outcomeCode = required(row, "outcome_code", "成果编码不能为空");
        String outcomeName = required(row, "outcome_name", "成果名称不能为空");
        String outcomeType = required(row, "outcome_type", "成果类型不能为空");
        String baseCreditText = required(row, "base_credit", "标准学分不能为空");
        String status = row.getByName("status");
        BigDecimal baseCredit;
        try {
            baseCredit = new BigDecimal(baseCreditText);
        } catch (NumberFormatException ex) {
            throw new BusinessException("标准学分格式错误");
        }
        if (baseCredit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("标准学分必须大于 0");
        }
        if (!StringUtils.hasText(status)) {
            status = Constants.STATUS_ENABLED;
        }
        if (!Constants.STATUS_ENABLED.equals(status) && !Constants.STATUS_DISABLED.equals(status)) {
            throw new BusinessException("状态只能为 enabled 或 disabled");
        }
        validateLength(outcomeCode, 64, "成果编码");
        validateLength(outcomeName, 200, "成果名称");
        validateLength(outcomeType, 64, "成果类型");
        rejectFormula(outcomeCode, "成果编码");
        rejectFormula(outcomeName, "成果名称");
        rejectFormula(outcomeType, "成果类型");
        LearnOutcomeCatalog catalog = new LearnOutcomeCatalog();
        catalog.setOutcomeCode(outcomeCode);
        catalog.setOutcomeName(outcomeName);
        catalog.setOutcomeType(outcomeType);
        catalog.setBaseCredit(baseCredit);
        catalog.setAuditStatus(Constants.STATUS_APPROVED);
        catalog.setStatus(status);
        return catalog;
    }

    private String required(CsvRow row, String field, String message) {
        String value = row.getByName(field);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private void validateLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new BusinessException(fieldName + "长度不能超过 " + maxLength);
        }
    }

    private void rejectFormula(String value, String fieldName) {
        if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) {
            throw new BusinessException(fieldName + "不能以公式字符开头");
        }
    }

    public record ImportResult(int totalRows,
                               int createdCount,
                               int updatedCount,
                               int skippedCount,
                               List<ImportError> errors) {
    }

    public record ImportError(int row, String message) {
    }
}
