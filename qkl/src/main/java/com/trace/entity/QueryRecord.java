package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消费者查询记录实体
 */
@Data
@TableName("query_record")
public class QueryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，匿名查询时为空 */
    private Long userId;

    /** 溯源码 */
    private String traceCode;

    /** 批次号 */
    private String batchNo;

    /** 查询时间 */
    private LocalDateTime queryTime;

    /** 查询IP */
    private String queryIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
