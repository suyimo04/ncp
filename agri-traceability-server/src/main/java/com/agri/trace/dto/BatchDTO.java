package com.agri.trace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchDTO {
    @NotNull(message = "经营主体不能为空")
    private Long producerId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String productCategory;
    private String originAddress;
    private LocalDate harvestTime;

    @NotNull(message = "批次重量不能为空")
    @DecimalMin(value = "0.01", message = "批次重量必须大于 0")
    private BigDecimal batchWeight;

    private String unit;
    private LocalDate expectedSaleTime;
    private String remark;
}
