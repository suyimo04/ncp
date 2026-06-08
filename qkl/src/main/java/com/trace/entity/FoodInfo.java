package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 食品档案实体
 */
@Data
@TableName("food_info")
public class FoodInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食品名称 */
    private String foodName;

    /** 分类ID */
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;

    /** 生产企业ID（enterprise_info的id） */
    private Long producerId;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 保质期（天） */
    private Integer shelfLife;

    /** 储存条件 */
    private String storageCondition;

    /** 食品图片 */
    private String foodImage;

    /** 食品描述 */
    private String description;

    /** 状态 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
