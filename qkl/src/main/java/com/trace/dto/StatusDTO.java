package com.trace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用状态修改参数（用户启用停用、公告发布下线等场景）
 */
@Data
public class StatusDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
