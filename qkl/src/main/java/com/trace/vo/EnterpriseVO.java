package com.trace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业信息返回对象
 */
@Data
public class EnterpriseVO {

    private Long id;
    private Long userId;
    private String username;
    private String enterpriseName;
    private String enterpriseType;
    private String licenseNo;
    private String licenseImage;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String description;
    private String auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
