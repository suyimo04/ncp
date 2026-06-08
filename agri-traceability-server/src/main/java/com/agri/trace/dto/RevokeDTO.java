package com.agri.trace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RevokeDTO {
    @NotBlank(message = "作废原因不能为空")
    private String revokeReason;
}
