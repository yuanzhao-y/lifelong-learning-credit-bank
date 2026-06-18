package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.entity.SysFile;
import com.zhousheng.llcb.service.TosStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    private final TosStorageService tosStorageService;

    public FileController(TosStorageService tosStorageService) {
        this.tosStorageService = tosStorageService;
    }

    @PostMapping("/upload")
    public ApiResponse<SysFile> upload(@RequestParam(defaultValue = "common") String bizType,
                                       @RequestParam(required = false) Long bizId,
                                       @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(tosStorageService.upload(bizType, bizId, file));
    }
}
