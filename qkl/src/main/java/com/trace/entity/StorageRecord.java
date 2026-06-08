package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入库记录实体
 */
@Data
@TableName("storage_record")
public class StorageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID */
    private Long batchId;

    /** 商家ID */
    private Long merchantId;

    /** 入库时间 */
    private LocalDateTime storageTime;

    /** 入库数量 */
    private Integer quantity;

    /** 存储位置 */
    private String storageLocation;

    /** 状态：STORED/ON_SHELF/SOLD_OUT */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
