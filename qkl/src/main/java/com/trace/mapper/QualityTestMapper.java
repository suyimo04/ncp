package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.QualityTest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质检记录Mapper
 */
@Mapper
public interface QualityTestMapper extends BaseMapper<QualityTest> {
}
