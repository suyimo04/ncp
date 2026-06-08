package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.SalesRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售记录Mapper
 */
@Mapper
public interface SalesRecordMapper extends BaseMapper<SalesRecord> {
}
