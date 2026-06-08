package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人信息变更申请实体
 */
@Data
@TableName("profile_change_request")
public class ProfileChangeRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 原姓名 */
    private String oldRealName;

    /** 新姓名 */
    private String newRealName;

    /** 原手机号 */
    private String oldPhone;

    /** 新手机号 */
    private String newPhone;

    /** 原邮箱 */
    private String oldEmail;

    /** 新邮箱 */
    private String newEmail;

    /** 变更原因 */
    private String reason;

    /** 状态：PENDING/APPROVED/REJECTED */
    private String status;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
