package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.StorageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库记录Mapper
 */
@Mapper
public interface StorageRecordMapper extends BaseMapper<StorageRecord> {
}
