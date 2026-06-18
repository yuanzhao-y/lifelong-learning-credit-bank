package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_hierarchy")
@EqualsAndHashCode(callSuper = true)
public class SysRoleHierarchy extends BaseEntity {
    private Long parentRoleId;
    private Long childRoleId;
}
