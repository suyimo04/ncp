package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.FoodCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食品分类Mapper
 */
@Mapper
public interface FoodCategoryMapper extends BaseMapper<FoodCategory> {
}
