package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产批次实体
 */
@Data
@TableName("production_batch")
public class ProductionBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次号 */
    private String batchNo;

    /** 食品ID */
    private Long foodId;

    /** 生产企业ID */
    private Long producerId;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 过期日期 */
    private LocalDate expiryDate;

    /** 生产数量 */
    private Integer quantity;

    /** 溯源码 */
    private String traceCode;

    /** 状态：CREATED/TESTED/SHIPPED/IN_TRANSIT/STORED/ON_SHELF/COMPLETED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
