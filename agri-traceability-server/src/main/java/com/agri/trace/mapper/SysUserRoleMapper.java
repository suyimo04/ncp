package com.agri.trace.mapper;

import com.agri.trace.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    @Delete("delete from sys_user_role where user_id = #{userId}")
    void deleteByUserIdPhysical(@Param("userId") Long userId);
}
