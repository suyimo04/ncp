package com.trace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核DTO（通用）
 */
@Data
public class AuditDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    /** APPROVED / REJECTED */
    @NotNull(message = "审核状态不能为空")
    private String status;

    /** 审核备注 */
    private String remark;
}
