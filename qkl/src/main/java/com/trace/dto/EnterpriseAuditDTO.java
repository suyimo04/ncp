package com.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企业审核参数
 */
@Data
public class EnterpriseAuditDTO {

    @NotNull(message = "企业ID不能为空")
    private Long id;

    /** 审核状态：APPROVED/REJECTED */
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;

    /** 审核备注 */
    private String auditRemark;
}
