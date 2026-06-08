package com.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.AuditDTO;
import com.trace.dto.ProfileChangeDTO;
import com.trace.entity.ProfileChangeRequest;
import com.trace.entity.SysUser;
import com.trace.mapper.ProfileChangeMapper;
import com.trace.mapper.SysUserMapper;
import com.trace.util.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 个人信息变更申请服务
 */
@Slf4j
@Service
public class ProfileChangeService {

    @Autowired
    private ProfileChangeMapper profileChangeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 提交个人信息变更申请
     */
    public void submitChange(ProfileChangeDTO dto) {
        Long userId = RequestContext.getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否有未审核的申请
        Long pendingCount = profileChangeMapper.selectCount(
                new LambdaQueryWrapper<ProfileChangeRequest>()
                        .eq(ProfileChangeRequest::getUserId, userId)
                        .eq(ProfileChangeRequest::getStatus, "PENDING")
        );
        if (pendingCount > 0) {
            throw new RuntimeException("您有待审核的变更申请，请等待审核完成后再提交");
        }

        ProfileChangeRequest request = new ProfileChangeRequest();
        request.setUserId(userId);
        request.setUsername(user.getUsername());
        request.setOldRealName(user.getRealName());
        request.setNewRealName(dto.getRealName());
        request.setOldPhone(user.getPhone());
        request.setNewPhone(dto.getPhone());
        request.setOldEmail(user.getEmail());
        request.setNewEmail(dto.getEmail());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");
        profileChangeMapper.insert(request);
    }

    /**
     * 查询变更申请列表（管理员）
     */
    public PageResult<ProfileChangeRequest> listRequests(Integer pageNum, Integer pageSize, String status) {
        Page<ProfileChangeRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProfileChangeRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ProfileChangeRequest::getStatus, status);
        }
        wrapper.orderByDesc(ProfileChangeRequest::getCreateTime);
        Page<ProfileChangeRequest> result = profileChangeMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 查询我的变更申请
     */
    public PageResult<ProfileChangeRequest> myRequests(Integer pageNum, Integer pageSize) {
        Long userId = RequestContext.getCurrentUserId();
        Page<ProfileChangeRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProfileChangeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProfileChangeRequest::getUserId, userId);
        wrapper.orderByDesc(ProfileChangeRequest::getCreateTime);
        Page<ProfileChangeRequest> result = profileChangeMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 审核变更申请（管理员）
     */
    @Transactional
    public void auditChange(AuditDTO dto) {
        ProfileChangeRequest request = profileChangeMapper.selectById(dto.getId());
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }

        request.setStatus(dto.getStatus());
        request.setAuditRemark(dto.getRemark());
        request.setAuditTime(LocalDateTime.now());
        request.setAuditorId(RequestContext.getCurrentUserId());
        profileChangeMapper.updateById(request);

        // 审核通过则更新用户信息
        if ("APPROVED".equals(dto.getStatus())) {
            SysUser user = sysUserMapper.selectById(request.getUserId());
            if (user != null) {
                if (request.getNewRealName() != null) {
                    user.setRealName(request.getNewRealName());
                }
                if (request.getNewPhone() != null) {
                    user.setPhone(request.getNewPhone());
                }
                if (request.getNewEmail() != null) {
                    user.setEmail(request.getNewEmail());
                }
                sysUserMapper.updateById(user);
                log.info("用户信息变更已生效: userId={}, username={}", user.getId(), user.getUsername());
            }
        }
    }
}
