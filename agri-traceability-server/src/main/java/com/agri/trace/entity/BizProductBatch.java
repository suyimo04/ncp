package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product_batch")
public class BizProductBatch extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String batchCode;
    private Long producerId;
    private String producerCode;
    private String producerName;
    private String productName;
    private String productCategory;
    private String originAddress;
    private LocalDate harvestTime;
    private BigDecimal batchWeight;
    private String unit;
    private LocalDate expectedSaleTime;
    private Integer batchStatus;
    private Integer qualityStatus;
    private String batchHash;
    private Integer chainStatus;
    private String remark;
}
