package com.trace.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次返回对象
 */
@Data
public class BatchVO {

    private Long id;
    private String batchNo;
    private Long foodId;
    private String foodName;
    private Long producerId;
    private String producerName;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private Integer quantity;
    private String traceCode;
    private String status;
    private LocalDateTime createTime;
}
