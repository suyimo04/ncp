package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹实体
 */
@Data
@TableName("logistics_track")
public class LogisticsTrack {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 运输记录ID */
    private Long logisticsRecordId;

    /** 位置 */
    private String location;

    /** 描述 */
    private String description;

    /** 记录时间 */
    private LocalDateTime trackTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
