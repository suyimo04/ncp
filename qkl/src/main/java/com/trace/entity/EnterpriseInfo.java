package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业信息实体
 */
@Data
@TableName("enterprise_info")
public class EnterpriseInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID */
    private Long userId;

    /** 企业名称 */
    private String enterpriseName;

    /** 企业类型：PRODUCER/LOGISTICS/MERCHANT */
    private String enterpriseType;

    /** 营业执照号 */
    private String licenseNo;

    /** 营业执照图片 */
    private String licenseImage;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 企业地址 */
    private String address;

    /** 企业简介 */
    private String description;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
