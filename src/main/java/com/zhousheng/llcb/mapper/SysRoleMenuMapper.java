package com.zhousheng.llcb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhousheng.llcb.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;

public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(Long roleId);
}
