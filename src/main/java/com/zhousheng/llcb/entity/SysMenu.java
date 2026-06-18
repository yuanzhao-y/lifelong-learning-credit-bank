package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_menu")
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends BaseEntity {
    private Long parentId;
    private String menuCode;
    private String menuName;
    private String menuType;
    private String routePath;
    private String component;
    private String routeName;
    private String icon;
    private String permissionCode;
    private Integer visible;
    private String status;
    private Integer sortNo;
}
