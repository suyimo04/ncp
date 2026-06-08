package com.agri.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuDTO {
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    private String icon;
    private String path;
    private String component;
    private String perms;
    private Integer sortOrder;
    private Integer status;
}
