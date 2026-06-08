package com.agri.trace.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PublicVerifyVO {
    private Boolean exists;
    private Boolean valid;
    private String verifyMessage;
    private String certificateCode;
    private String productName;
    private String productCategory;
    private String producerName;
    private String originAddress;
    private LocalDate harvestTime;
    private String reportCode;
    private String testAgency;
    private String conclusion;
    private LocalDateTime issueTime;
    private LocalDate expireTime;
    private String certificateHash;
    private String txHash;
    private Long blockNumber;
    private Boolean batchHashMatched;
    private Boolean reportHashMatched;
    private Boolean certificateHashMatched;
}
