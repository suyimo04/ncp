package com.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物流状态更新参数
 */
@Data
public class LogisticsStatusDTO {

    @NotNull(message = "运输记录ID不能为空")
    private Long id;

    @NotBlank(message = "状态不能为空")
    private String status;
}
