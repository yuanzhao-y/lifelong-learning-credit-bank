package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_api")
@EqualsAndHashCode(callSuper = true)
public class SysRoleApi extends BaseEntity {
    private Long roleId;
    private Long apiId;
}
