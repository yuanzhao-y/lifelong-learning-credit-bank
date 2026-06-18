package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_menu")
@EqualsAndHashCode(callSuper = true)
public class SysRoleMenu extends BaseEntity {
    private Long roleId;
    private Long menuId;
}
