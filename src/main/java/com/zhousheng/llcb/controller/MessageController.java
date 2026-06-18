package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.config.MybatisPlusConfig;
import com.zhousheng.llcb.entity.SysMessage;
import com.zhousheng.llcb.entity.SysMessageReceiver;
import com.zhousheng.llcb.mapper.SysMessageMapper;
import com.zhousheng.llcb.mapper.SysMessageReceiverMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import com.zhousheng.llcb.service.MessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {

    private final SysMessageMapper messageMapper;
    private final SysMessageReceiverMapper receiverMapper;
    private final MessageService messageService;

    public MessageController(SysMessageMapper messageMapper,
                             SysMessageReceiverMapper receiverMapper,
                             MessageService messageService) {
        this.messageMapper = messageMapper;
        this.receiverMapper = receiverMapper;
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public ApiResponse<Page<SysMessageReceiver>> myMessages(@RequestParam(defaultValue = "1") long page,
                                                            @RequestParam(defaultValue = "10") long size,
                                                            @RequestParam(required = false) String readStatus) {
        return ApiResponse.ok(receiverMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysMessageReceiver>()
                        .eq(SysMessageReceiver::getReceiverId, SecurityUtils.currentUserId())
                        .eq(StringUtils.hasText(readStatus), SysMessageReceiver::getReadStatus, readStatus)
                        .eq(SysMessageReceiver::getReceiverDeleted, 0)
                        .orderByDesc(SysMessageReceiver::getCreatedAt)));
    }

    @GetMapping("/messages/{id}")
    public ApiResponse<SysMessage> detail(@PathVariable Long id) {
        messageService.markRead(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok(messageMapper.selectById(id));
    }

    @PostMapping("/messages/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        messageService.markRead(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok();
    }

    @PostMapping("/messages/read-all")
    public ApiResponse<Void> markAllRead() {
        messageService.markAllRead(SecurityUtils.currentUserId());
        return ApiResponse.ok();
    }

    @GetMapping("/admin/messages")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<SysMessage>> sentRecords(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(messageMapper.selectPage(MybatisPlusConfig.page(page, size),
                new LambdaQueryWrapper<SysMessage>().orderByDesc(SysMessage::getSentAt)));
    }
}
