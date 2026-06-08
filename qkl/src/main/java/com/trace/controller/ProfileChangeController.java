package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.common.OperationLog;
import com.trace.dto.AuditDTO;
import com.trace.dto.ProfileChangeDTO;
import com.trace.entity.ProfileChangeRequest;
import com.trace.service.ProfileChangeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 个人信息变更接口
 */
@RestController
@RequestMapping("/api/profile-change")
public class ProfileChangeController {

    @Autowired
    private ProfileChangeService profileChangeService;

    /** 提交个人信息变更申请 */
    @OperationLog("提交信息变更申请")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody ProfileChangeDTO dto) {
        profileChangeService.submitChange(dto);
        return Result.success();
    }

    /** 查询我的变更申请 */
    @GetMapping("/my")
    public Result<PageResult<ProfileChangeRequest>> myRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(profileChangeService.myRequests(pageNum, pageSize));
    }

    /** 管理员查询所有变更申请 */
    @GetMapping("/list")
    public Result<PageResult<ProfileChangeRequest>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(profileChangeService.listRequests(pageNum, pageSize, status));
    }

    /** 管理员审核变更申请 */
    @OperationLog("审核信息变更申请")
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody @Valid AuditDTO dto) {
        profileChangeService.auditChange(dto);
        return Result.success();
    }
}
