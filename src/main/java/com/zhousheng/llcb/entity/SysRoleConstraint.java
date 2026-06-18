package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_constraint")
@EqualsAndHashCode(callSuper = true)
public class SysRoleConstraint extends BaseEntity {
    private String constraintCode;
    private String constraintName;
    private String constraintType;
    private String roleIdsJson;
    private Integer maxActiveCount;
    private String status;
}
