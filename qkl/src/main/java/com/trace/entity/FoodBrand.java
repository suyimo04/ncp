package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 品牌实体
 */
@Data
@TableName("food_brand")
public class FoodBrand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 品牌名称 */
    private String brandName;

    /** 品牌Logo */
    private String brandLogo;

    /** 品牌描述 */
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
