package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_api_permission")
@EqualsAndHashCode(callSuper = true)
public class SysApiPermission extends BaseEntity {
    private String apiCode;
    private String apiName;
    private String module;
    private String httpMethod;
    private String pathPattern;
    private String status;
}
