package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_sms_code")
@EqualsAndHashCode(callSuper = true)
public class SysSmsCode extends BaseEntity {
    private String phoneHash;
    private String scene;
    private String codeHash;
    private LocalDateTime expireAt;
    private LocalDateTime usedAt;
    private Integer verifyFailCount;
    private String requestIp;
}
