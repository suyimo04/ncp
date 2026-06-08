package com.agri.trace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    @NotBlank(message = "账号不能为空")
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private List<Long> roleIds;
}
