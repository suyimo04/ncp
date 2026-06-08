package com.agri.trace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TestReportDTO {
    @NotNull(message = "批次不能为空")
    private Long batchId;
    private String testAgency;
    private String testType;
    private LocalDate testDate;
    private String testItems;
    private String testResult;
    private String conclusion;
    private String reportFileUrl;
    private String reportFileHash;
}
