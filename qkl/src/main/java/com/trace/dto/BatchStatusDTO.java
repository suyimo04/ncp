package com.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次状态更新参数
 */
@Data
public class BatchStatusDTO {

    @NotNull(message = "批次ID不能为空")
    private Long id;

    /** 目标状态 */
    @NotBlank(message = "状态不能为空")
    private String status;
}
