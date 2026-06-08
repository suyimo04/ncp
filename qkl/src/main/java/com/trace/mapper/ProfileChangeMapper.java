package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.ProfileChangeRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人信息变更申请Mapper
 */
@Mapper
public interface ProfileChangeMapper extends BaseMapper<ProfileChangeRequest> {
}
