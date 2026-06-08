package com.trace.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流运输参数
 */
@Data
public class LogisticsDTO {

    private Long id;

    private Long batchId;

    private Long senderId;

    private Long receiverId;

    private String senderAddress;

    private String receiverAddress;

    private LocalDateTime shipTime;

    private String temperature;

    private String transportType;

    private String remark;
}
