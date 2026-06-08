package com.agri.trace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditDTO {
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    private String opinion;
}
