package com.trace.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 销售记录参数
 */
@Data
public class SalesDTO {

    private Long id;

    private Long storageId;

    private LocalDateTime salesTime;

    private Integer quantity;

    private String buyerInfo;

    private java.math.BigDecimal price;

    private String remark;
}
