package com.trace.dto;

import lombok.Data;

/**
 * 个人信息变更申请DTO
 */
@Data
public class ProfileChangeDTO {

    /** 新姓名 */
    private String realName;

    /** 新手机号 */
    private String phone;

    /** 新邮箱 */
    private String email;

    /** 变更原因 */
    private String reason;
}
