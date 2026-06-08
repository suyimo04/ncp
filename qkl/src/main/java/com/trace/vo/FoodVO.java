package com.trace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 食品档案返回对象
 */
@Data
public class FoodVO {

    private Long id;
    private String foodName;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long producerId;
    private String producerName;
    private String specification;
    private String unit;
    private Integer shelfLife;
    private String storageCondition;
    private String foodImage;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
