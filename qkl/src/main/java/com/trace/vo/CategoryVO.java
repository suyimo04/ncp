package com.trace.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树形节点
 */
@Data
public class CategoryVO {

    private Long id;
    private String categoryName;
    private Long parentId;
    private Integer sort;
    private Integer status;

    /** 子分类 */
    private List<CategoryVO> children;
}
