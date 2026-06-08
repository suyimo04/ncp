package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.UserDTO;
import com.trace.entity.SysUser;
import com.trace.exception.BusinessException;
import com.trace.mapper.SysUserMapper;
import com.trace.util.RequestContext;
import com.trace.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserVO> listUsers(Integer pageNum, Integer pageSize, String keyword, String role, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名或姓名）
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (StrUtil.isNotBlank(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<UserVO> voList = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    /**
     * 获取用户详情
     */
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    /**
     * 新增用户（管理员操作）
     */
    public void addUser(UserDTO dto) {
        // 只有管理员能新增用户
        String currentRole = RequestContext.getCurrentRole();
        if (!"ADMIN".equals(currentRole)) {
            throw new BusinessException(403, "没有操作权限");
        }
        // 校验用户名
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtil.copyProperties(dto, user);
        // 默认密码
        String pwd = StrUtil.isBlank(dto.getPassword()) ? "admin123" : dto.getPassword();
        user.setPassword(passwordEncoder.encode(pwd));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userMapper.insert(user);
        log.info("管理员新增用户: {}", dto.getUsername());
    }

    /**
     * 修改用户信息
     */
    public void updateUser(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 允许修改的字段
        if (StrUtil.isNotBlank(dto.getRealName())) user.setRealName(dto.getRealName());
        if (StrUtil.isNotBlank(dto.getPhone())) user.setPhone(dto.getPhone());
        if (StrUtil.isNotBlank(dto.getEmail())) user.setEmail(dto.getEmail());
        if (StrUtil.isNotBlank(dto.getRole())) user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (StrUtil.isNotBlank(dto.getAvatar())) user.setAvatar(dto.getAvatar());
        // 密码修改（管理员重置密码）
        if (StrUtil.isNotBlank(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 不能删除自己
        Long currentUserId = RequestContext.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException("不能删除自己的账号");
        }
        userMapper.deleteById(id);
        log.info("删除用户: {} (ID:{})", user.getUsername(), id);
    }

    /**
     * 修改用户状态（启用/停用）
     */
    public void changeStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("修改用户 {} 状态为: {}", user.getUsername(), status == 1 ? "启用" : "停用");
    }
}
