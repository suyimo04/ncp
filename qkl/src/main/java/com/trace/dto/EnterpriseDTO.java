package com.trace.dto;

import lombok.Data;

/**
 * 企业信息修改参数
 */
@Data
public class EnterpriseDTO {

    private Long id;

    private Long userId;

    private String enterpriseName;

    private String enterpriseType;

    private String licenseNo;

    private String licenseImage;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private String description;
}
