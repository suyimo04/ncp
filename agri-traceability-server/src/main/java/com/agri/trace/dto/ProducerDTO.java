package com.agri.trace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProducerDTO {
    private Long userId;

    @NotBlank(message = "主体名称不能为空")
    private String producerName;

    private String producerType;

    @NotBlank(message = "统一社会信用代码不能为空")
    private String creditCode;

    private String legalPerson;
    private String contactPhone;
    private String townName;
    private String address;
    private String businessScope;
    private String remark;
}
