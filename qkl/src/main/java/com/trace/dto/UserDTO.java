package com.trace.dto;

import lombok.Data;

/**
 * 用户新增/修改参数
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String role;

    private Integer status;

    private String avatar;
}
