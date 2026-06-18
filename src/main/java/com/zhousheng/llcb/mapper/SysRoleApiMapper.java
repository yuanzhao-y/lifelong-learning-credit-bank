package com.zhousheng.llcb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhousheng.llcb.entity.SysRoleApi;
import org.apache.ibatis.annotations.Delete;

public interface SysRoleApiMapper extends BaseMapper<SysRoleApi> {
    @Delete("DELETE FROM sys_role_api WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(Long roleId);
}
