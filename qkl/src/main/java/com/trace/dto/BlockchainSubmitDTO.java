package com.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 上链请求参数
 */
@Data
public class BlockchainSubmitDTO {

    /** 业务类型：BATCH/TEST/LOGISTICS/STORAGE */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /** 业务ID */
    @NotNull(message = "业务ID不能为空")
    private Long businessId;
}
