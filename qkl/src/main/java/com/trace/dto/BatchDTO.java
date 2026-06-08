package com.trace.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 批次参数
 */
@Data
public class BatchDTO {

    private Long id;

    private Long foodId;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private Integer quantity;
}
