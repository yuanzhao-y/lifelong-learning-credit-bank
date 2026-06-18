package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_message")
@EqualsAndHashCode(callSuper = true)
public class SysMessage extends BaseEntity {
    private String messageTitle;
    private String messageContent;
    private String messageType;
    private String triggerScene;
    private Long senderId;
    private String bizType;
    private Long bizId;
    private LocalDateTime sentAt;
}
