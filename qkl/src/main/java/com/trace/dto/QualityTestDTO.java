package com.trace.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 质检记录参数
 */
@Data
public class QualityTestDTO {

    private Long id;

    private Long batchId;

    private LocalDate testDate;

    /** QUALIFIED / UNQUALIFIED */
    private String testResult;

    private String testReport;

    private String tester;

    private String testOrg;

    private String remark;
}
