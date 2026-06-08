package com.agri.trace.service.impl;

import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.MenuDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.UserDTO;
import com.agri.trace.entity.SysMenu;
import com.agri.trace.entity.SysRole;
import com.agri.trace.entity.SysUser;
import com.agri.trace.entity.SysUserRole;
import com.agri.trace.mapper.SysMenuMapper;
import com.agri.trace.mapper.SysRoleMapper;
import com.agri.trace.mapper.SysUserMapper;
import com.agri.trace.mapper.SysUserRoleMapper;
import com.agri.trace.service.SystemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<SysUser> userPage(PageQueryDTO query) {
        SecurityUtil.requireRole("ADMIN");
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysUser::getUsername, query.getKeyword()).or().like(SysUser::getRealName, query.getKeyword());
        }
        IPage<SysUser> page = userMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper.orderByDesc(SysUser::getCreateTime));
        page.getRecords().forEach(user -> user.setPassword(null));
        return page;
    }

    @Override
    public SysUser createUser(UserDTO dto) {
        SecurityUtil.requireRole("ADMIN");
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("账号已存在");
        }
        SysUser user = new SysUser();
        fill(user, dto);
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "admin123"));
        userMapper.insert(user);
        saveRoles(user.getId(), dto.getRoleIds());
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser updateUser(Long id, UserDTO dto) {
        SecurityUtil.requireRole("ADMIN");
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        fill(user, dto);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
        saveRoles(id, dto.getRoleIds());
        user.setPassword(null);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        SecurityUtil.requireRole("ADMIN");
        userMapper.deleteById(id);
    }

    @Override
    public List<SysRole> roles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    @Override
    public List<SysMenu> menus() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getSortOrder));
    }

    @Override
    public SysMenu createMenu(MenuDTO dto) {
        SecurityUtil.requireRole("ADMIN");
        SysMenu menu = new SysMenu();
        fillMenu(menu, dto);
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    public SysMenu updateMenu(Long id, MenuDTO dto) {
        SecurityUtil.requireRole("ADMIN");
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        fillMenu(menu, dto);
        menuMapper.updateById(menu);
        return menu;
    }

    @Override
    public void deleteMenu(Long id) {
        SecurityUtil.requireRole("ADMIN");
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (children > 0) {
            throw new BusinessException("请先删除子菜单，再删除目录");
        }
        menuMapper.deleteById(id);
    }

    private void fill(SysUser user, UserDTO dto) {
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }

    private void fillMenu(SysMenu menu, MenuDTO dto) {
        menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setIcon(dto.getIcon());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPerms(dto.getPerms());
        menu.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        menu.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        // 角色关联是纯关系数据，用物理删除避免逻辑删除后再次分配同一角色撞唯一索引。
        userRoleMapper.deleteByUserIdPhysical(userId);
        if (roleIds != null) {
            roleIds.forEach(roleId -> userRoleMapper.insert(new SysUserRole(userId, roleId)));
        }
    }
}
