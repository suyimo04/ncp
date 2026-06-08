package com.trace.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入库参数
 */
@Data
public class StorageDTO {

    private Long id;

    private Long batchId;

    private LocalDateTime storageTime;

    private Integer quantity;

    private String storageLocation;
}
