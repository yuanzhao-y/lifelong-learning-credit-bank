package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_feedback")
@EqualsAndHashCode(callSuper = true)
public class SysFeedback extends BaseEntity {
    private String feedbackNo;
    private Long userId;
    private String title;
    private String content;
    private String contact;
    private String status;
    private String replyContent;
    private Long replyAdminId;
    private LocalDateTime repliedAt;
}
