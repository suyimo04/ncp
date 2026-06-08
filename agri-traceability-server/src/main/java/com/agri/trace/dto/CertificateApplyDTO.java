package com.agri.trace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateApplyDTO {
    @NotNull(message = "批次不能为空")
    private Long batchId;

    @NotNull(message = "检测报告不能为空")
    private Long reportId;
}
