package com.trace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回对象
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private String avatar;
    private LocalDateTime createTime;

    /** 关联的企业名称（企业用户才有） */
    private String enterpriseName;
    /** 企业审核状态 */
    private String auditStatus;
}
