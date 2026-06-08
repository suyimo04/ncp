package com.agri.trace.service;

import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.MenuDTO;
import com.agri.trace.dto.UserDTO;
import com.agri.trace.entity.SysMenu;
import com.agri.trace.entity.SysRole;
import com.agri.trace.entity.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface SystemService {
    IPage<SysUser> userPage(PageQueryDTO query);

    SysUser createUser(UserDTO dto);

    SysUser updateUser(Long id, UserDTO dto);

    void deleteUser(Long id);

    List<SysRole> roles();

    List<SysMenu> menus();

    SysMenu createMenu(MenuDTO dto);

    SysMenu updateMenu(Long id, MenuDTO dto);

    void deleteMenu(Long id);
}
