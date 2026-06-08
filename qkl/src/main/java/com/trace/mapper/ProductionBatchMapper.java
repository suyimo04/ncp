package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.ProductionBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产批次Mapper
 */
@Mapper
public interface ProductionBatchMapper extends BaseMapper<ProductionBatch> {
}
