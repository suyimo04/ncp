package com.agri.trace.service.impl;

import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.JwtUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.LoginDTO;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.SysMenu;
import com.agri.trace.entity.SysRole;
import com.agri.trace.entity.SysRoleMenu;
import com.agri.trace.entity.SysUser;
import com.agri.trace.entity.SysUserRole;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.SysMenuMapper;
import com.agri.trace.mapper.SysRoleMenuMapper;
import com.agri.trace.mapper.SysRoleMapper;
import com.agri.trace.mapper.SysUserMapper;
import com.agri.trace.mapper.SysUserRoleMapper;
import com.agri.trace.service.AuthService;
import com.agri.trace.vo.AuthInfoVO;
import com.agri.trace.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final BizProducerMapper producerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        List<String> roles = rolesOf(user.getId());
        return new LoginVO(jwtUtil.generateToken(user.getId(), user.getUsername(), roles));
    }

    @Override
    public AuthInfoVO info() {
        Long userId = SecurityUtil.userId();
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        AuthInfoVO vo = new AuthInfoVO();
        vo.setUserInfo(user);
        vo.setRoles(rolesOf(userId));
        List<Long> roleIds = roleIdsOf(userId);
        List<Long> menuIds = roleIds.isEmpty()
                ? List.of()
                : roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().toList();
        vo.setMenus(menuIds.isEmpty()
                ? List.of()
                : menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder)));
        BizProducer producer = producerMapper.selectOne(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getUserId, userId));
        vo.setProducerId(producer == null ? null : producer.getId());
        return vo;
    }

    private List<String> rolesOf(Long userId) {
        List<Long> roleIds = roleIdsOf(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream().map(SysRole::getRoleCode).toList();
    }

    private List<Long> roleIdsOf(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }
}
