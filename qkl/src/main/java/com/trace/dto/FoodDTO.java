package com.trace.dto;

import lombok.Data;

/**
 * 食品档案参数
 */
@Data
public class FoodDTO {

    private Long id;

    private String foodName;

    private Long categoryId;

    private Long brandId;

    private String specification;

    private String unit;

    private Integer shelfLife;

    private String storageCondition;

    private String foodImage;

    private String description;
}
