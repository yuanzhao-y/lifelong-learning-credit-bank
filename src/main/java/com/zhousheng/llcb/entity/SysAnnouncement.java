package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_announcement")
@EqualsAndHashCode(callSuper = true)
public class SysAnnouncement extends BaseEntity {
    private String title;
    private String content;
    private String status;
    private LocalDateTime publishAt;
    private Long publisherId;
    private Integer sortNo;
    private Long viewCount;
}
