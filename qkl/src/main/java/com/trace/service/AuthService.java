package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trace.dto.LoginDTO;
import com.trace.dto.PasswordDTO;
import com.trace.dto.RegisterDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.SysUser;
import com.trace.exception.BusinessException;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.SysUserMapper;
import com.trace.security.JwtTokenProvider;
import com.trace.util.RequestContext;
import com.trace.vo.LoginVO;
import com.trace.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证相关服务
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        // 查找用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        // 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    /**
     * 用户注册（支持企业用户和消费者）
     */
    @Transactional
    public void register(RegisterDTO dto) {
        // 校验用户名是否已存在
        Long existed = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (existed > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 注册时不允许注册管理员
        if ("ADMIN".equals(dto.getRole())) {
            throw new BusinessException("不允许注册管理员账号");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(1);
        userMapper.insert(user);

        // 企业用户需要同时创建企业信息
        if ("PRODUCER".equals(dto.getRole()) || "LOGISTICS".equals(dto.getRole()) || "MERCHANT".equals(dto.getRole())) {
            if (StrUtil.isBlank(dto.getEnterpriseName())) {
                throw new BusinessException("企业名称不能为空");
            }
            EnterpriseInfo info = new EnterpriseInfo();
            info.setUserId(user.getId());
            info.setEnterpriseName(dto.getEnterpriseName());
            info.setEnterpriseType(dto.getEnterpriseType() != null ? dto.getEnterpriseType() : dto.getRole());
            info.setLicenseNo(dto.getLicenseNo());
            info.setLicenseImage(dto.getLicenseImage());
            info.setContactPerson(dto.getContactPerson());
            info.setContactPhone(dto.getContactPhone());
            info.setAddress(dto.getAddress());
            info.setDescription(dto.getEnterpriseDesc());
            info.setAuditStatus("PENDING"); // 新注册的企业需要审核
            enterpriseInfoMapper.insert(info);
        }

        log.info("新用户注册成功: {}, 角色: {}", dto.getUsername(), dto.getRole());
    }

    /**
     * 获取当前登录用户信息
     */
    public UserVO getCurrentUserInfo() {
        Long userId = RequestContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);

        // 如果是企业用户，查关联的企业信息
        if (!"ADMIN".equals(user.getRole()) && !"CONSUMER".equals(user.getRole())) {
            EnterpriseInfo enterprise = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>()
                            .eq(EnterpriseInfo::getUserId, userId)
            );
            if (enterprise != null) {
                vo.setEnterpriseName(enterprise.getEnterpriseName());
                vo.setAuditStatus(enterprise.getAuditStatus());
            }
        }
        return vo;
    }

    /**
     * 修改密码
     */
    public void changePassword(PasswordDTO dto) {
        Long userId = RequestContext.getCurrentUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户 {} 修改了密码", user.getUsername());
    }
}
