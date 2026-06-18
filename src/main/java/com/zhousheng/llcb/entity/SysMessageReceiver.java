package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_message_receiver")
@EqualsAndHashCode(callSuper = true)
public class SysMessageReceiver extends BaseEntity {
    private Long messageId;
    private Long receiverId;
    private String readStatus;
    private LocalDateTime readAt;
    private Integer receiverDeleted;
}
