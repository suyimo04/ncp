package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.EnterpriseAuditDTO;
import com.trace.dto.EnterpriseDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.SysUser;
import com.trace.exception.BusinessException;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.SysUserMapper;
import com.trace.util.RequestContext;
import com.trace.vo.EnterpriseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业管理服务
 */
@Slf4j
@Service
public class EnterpriseService {

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 分页查询企业列表
     */
    public PageResult<EnterpriseVO> listEnterprises(Integer pageNum, Integer pageSize,
                                                     String enterpriseType, String auditStatus, String keyword) {
        Page<EnterpriseInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EnterpriseInfo> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(enterpriseType)) {
            wrapper.eq(EnterpriseInfo::getEnterpriseType, enterpriseType);
        }
        if (StrUtil.isNotBlank(auditStatus)) {
            wrapper.eq(EnterpriseInfo::getAuditStatus, auditStatus);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(EnterpriseInfo::getEnterpriseName, keyword);
        }
        wrapper.orderByDesc(EnterpriseInfo::getCreateTime);

        Page<EnterpriseInfo> result = enterpriseInfoMapper.selectPage(page, wrapper);
        List<EnterpriseVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    /**
     * 获取企业详情
     */
    public EnterpriseVO getById(Long id) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException("企业不存在");
        }
        return toVO(info);
    }

    /**
     * 审核企业
     */
    public void audit(EnterpriseAuditDTO dto) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(dto.getId());
        if (info == null) {
            throw new BusinessException("企业不存在");
        }
        info.setAuditStatus(dto.getAuditStatus());
        info.setAuditRemark(dto.getAuditRemark());
        info.setAuditTime(LocalDateTime.now());
        enterpriseInfoMapper.updateById(info);
        log.info("审核企业 {} -> {}", info.getEnterpriseName(), dto.getAuditStatus());
    }

    /**
     * 获取当前用户的企业信息
     */
    public EnterpriseVO getMyEnterprise() {
        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo info = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (info == null) {
            throw new BusinessException("未找到企业信息");
        }
        return toVO(info);
    }

    /**
     * 修改当前用户的企业信息
     */
    public void updateMyEnterprise(EnterpriseDTO dto) {
        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo info = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (info == null) {
            throw new BusinessException("未找到企业信息");
        }
        if (StrUtil.isNotBlank(dto.getEnterpriseName())) info.setEnterpriseName(dto.getEnterpriseName());
        if (StrUtil.isNotBlank(dto.getLicenseNo())) info.setLicenseNo(dto.getLicenseNo());
        if (StrUtil.isNotBlank(dto.getLicenseImage())) info.setLicenseImage(dto.getLicenseImage());
        if (StrUtil.isNotBlank(dto.getContactPerson())) info.setContactPerson(dto.getContactPerson());
        if (StrUtil.isNotBlank(dto.getContactPhone())) info.setContactPhone(dto.getContactPhone());
        if (StrUtil.isNotBlank(dto.getAddress())) info.setAddress(dto.getAddress());
        if (StrUtil.isNotBlank(dto.getDescription())) info.setDescription(dto.getDescription());
        enterpriseInfoMapper.updateById(info);
    }

    /**
     * 根据用户ID获取企业信息
     */
    public EnterpriseInfo getByUserId(Long userId) {
        return enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
    }

    /**
     * 启用/停用企业（修改关联用户状态）
     */
    public void changeStatus(Long enterpriseId, Integer status) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(enterpriseId);
        if (info == null) {
            throw new BusinessException("企业不存在");
        }
        SysUser user = userMapper.selectById(info.getUserId());
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        log.info("企业 {} 状态变更为: {}", info.getEnterpriseName(), status == 1 ? "启用" : "停用");
    }

    /**
     * 管理员新增企业
     */
    public void addEnterprise(EnterpriseDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException("必须指定关联的用户账号");
        }
        // 检查该用户是否已经关联企业
        EnterpriseInfo exist = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, dto.getUserId())
        );
        if (exist != null) {
            throw new BusinessException("该关联账号已绑定其他企业");
        }
        EnterpriseInfo info = new EnterpriseInfo();
        BeanUtil.copyProperties(dto, info);
        info.setAuditStatus("APPROVED"); // 管理员添加的直接算作已通过审核
        info.setAuditTime(LocalDateTime.now());
        info.setCreateTime(LocalDateTime.now());
        enterpriseInfoMapper.insert(info);
    }

    /**
     * 管理员修改企业信息
     */
    public void updateEnterprise(EnterpriseDTO dto) {
        if (dto.getId() == null) throw new BusinessException("缺少企业ID");
        EnterpriseInfo info = enterpriseInfoMapper.selectById(dto.getId());
        if (info == null) throw new BusinessException("企业不存在");

        if (dto.getUserId() != null && !dto.getUserId().equals(info.getUserId())) {
            // 变了 userId，检查冲突
            EnterpriseInfo exist = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>()
                            .eq(EnterpriseInfo::getUserId, dto.getUserId())
                            .ne(EnterpriseInfo::getId, dto.getId())
            );
            if (exist != null) throw new BusinessException("该关联账号已绑定其他企业");
            info.setUserId(dto.getUserId());
        }

        if (StrUtil.isNotBlank(dto.getEnterpriseName())) info.setEnterpriseName(dto.getEnterpriseName());
        if (StrUtil.isNotBlank(dto.getEnterpriseType())) info.setEnterpriseType(dto.getEnterpriseType());
        if (StrUtil.isNotBlank(dto.getLicenseNo())) info.setLicenseNo(dto.getLicenseNo());
        if (StrUtil.isNotBlank(dto.getLicenseImage())) info.setLicenseImage(dto.getLicenseImage());
        if (StrUtil.isNotBlank(dto.getContactPerson())) info.setContactPerson(dto.getContactPerson());
        if (StrUtil.isNotBlank(dto.getContactPhone())) info.setContactPhone(dto.getContactPhone());
        if (StrUtil.isNotBlank(dto.getAddress())) info.setAddress(dto.getAddress());
        if (StrUtil.isNotBlank(dto.getDescription())) info.setDescription(dto.getDescription());
        info.setUpdateTime(LocalDateTime.now());
        enterpriseInfoMapper.updateById(info);
    }

    /**
     * 管理员删除企业
     */
    public void deleteEnterprise(Long id) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(id);
        if (info == null) throw new BusinessException("企业不存在");
        enterpriseInfoMapper.deleteById(id);
        log.info("删除了企业: {}", info.getEnterpriseName());
    }

    private EnterpriseVO toVO(EnterpriseInfo info) {
        EnterpriseVO vo = new EnterpriseVO();
        BeanUtil.copyProperties(info, vo);
        // 查关联的用户名
        SysUser user = userMapper.selectById(info.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }
        return vo;
    }
}
