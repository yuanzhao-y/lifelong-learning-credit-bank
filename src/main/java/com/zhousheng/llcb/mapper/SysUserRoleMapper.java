package com.zhousheng.llcb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhousheng.llcb.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;

public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(Long userId);
}
