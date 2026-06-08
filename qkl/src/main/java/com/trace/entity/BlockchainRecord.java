package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区块链上链记录实体
 */
@Data
@TableName("blockchain_record")
public class BlockchainRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型：BATCH/TEST/LOGISTICS/STORAGE */
    private String businessType;

    /** 业务ID */
    private Long businessId;

    /** 批次号 */
    private String batchNo;

    /** 交易哈希 */
    private String txHash;

    /** 区块号 */
    private Long blockNumber;

    /** 数据摘要哈希 */
    private String dataHash;

    /** 状态：PENDING/SUCCESS/FAILED */
    private String status;

    /** 上链时间 */
    private LocalDateTime chainTime;

    /** 错误信息 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
