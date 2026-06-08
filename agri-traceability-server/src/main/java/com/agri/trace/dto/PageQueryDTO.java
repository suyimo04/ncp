package com.agri.trace.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
}
