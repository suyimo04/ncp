package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.FoodInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食品档案Mapper
 */
@Mapper
public interface FoodInfoMapper extends BaseMapper<FoodInfo> {
}
