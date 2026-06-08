package com.agri.trace.vo;

import com.agri.trace.entity.SysMenu;
import com.agri.trace.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class AuthInfoVO {
    private SysUser userInfo;
    private List<String> roles;
    private List<SysMenu> menus;
    private Long producerId;
}
