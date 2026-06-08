package com.trace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色，注册时只允许：PRODUCER/LOGISTICS/MERCHANT/CONSUMER */
    @NotBlank(message = "请选择角色类型")
    private String role;

    // 以下是企业注册时的字段
    /** 企业名称 */
    private String enterpriseName;

    /** 企业类型 */
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
    private String enterpriseDesc;
}
