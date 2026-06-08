package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.QueryRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 查询记录Mapper
 */
@Mapper
public interface QueryRecordMapper extends BaseMapper<QueryRecord> {
}
