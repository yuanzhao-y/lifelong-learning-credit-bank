package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_file")
@EqualsAndHashCode(callSuper = true)
public class SysFile extends BaseEntity {
    private String bizType;
    private Long bizId;
    private Long uploaderId;
    private String originalName;
    private String fileName;
    private String bucketName;
    private String objectKey;
    private String fileUrl;
    private String contentType;
    private String fileExt;
    private Long fileSize;
    private String fileMd5;
    private String status;
}
