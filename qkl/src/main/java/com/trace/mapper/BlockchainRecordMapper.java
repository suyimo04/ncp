package com.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.entity.BlockchainRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区块链记录Mapper
 */
@Mapper
public interface BlockchainRecordMapper extends BaseMapper<BlockchainRecord> {
}
