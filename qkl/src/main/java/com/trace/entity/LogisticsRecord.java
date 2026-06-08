package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流运输记录实体
 */
@Data
@TableName("logistics_record")
public class LogisticsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID */
    private Long batchId;

    /** 物流企业ID */
    private Long logisticsId;

    /** 发货方ID */
    private Long senderId;

    /** 收货方ID */
    private Long receiverId;

    /** 发货地址 */
    private String senderAddress;

    /** 收货地址 */
    private String receiverAddress;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 到达时间 */
    private LocalDateTime arriveTime;

    /** 状态：PENDING/IN_TRANSIT/ARRIVED/SIGNED */
    private String status;

    /** 运输温度 */
    private String temperature;

    /** 运输方式 */
    private String transportType;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
