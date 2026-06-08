package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 销售记录实体
 */
@Data
@TableName("sales_record")
public class SalesRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库记录ID */
    private Long storageId;

    /** 销售时间 */
    private LocalDateTime salesTime;

    /** 销售数量 */
    private Integer quantity;

    /** 买家信息 */
    private String buyerInfo;

    /** 销售单价 */
    private java.math.BigDecimal price;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
