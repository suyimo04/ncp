package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.common.OperationLog;
import com.trace.dto.EnterpriseAuditDTO;
import com.trace.dto.EnterpriseDTO;
import com.trace.dto.StatusDTO;
import com.trace.service.EnterpriseService;
import com.trace.vo.EnterpriseVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企业管理接口
 */
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Autowired
    private EnterpriseService enterpriseService;

    /** 企业列表 */
    @GetMapping("/list")
    public Result<PageResult<EnterpriseVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String enterpriseType,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String keyword) {
        return Result.success(enterpriseService.listEnterprises(pageNum, pageSize, enterpriseType, auditStatus, keyword));
    }

    /** 企业详情 */
    @GetMapping("/{id}")
    public Result<EnterpriseVO> detail(@PathVariable Long id) {
        return Result.success(enterpriseService.getById(id));
    }

    /** 审核企业 */
    @OperationLog("审核企业")
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody @Valid EnterpriseAuditDTO dto) {
        enterpriseService.audit(dto);
        return Result.success();
    }

    /** 管理员新增企业 */
    @OperationLog("新增企业")
    @PostMapping
    public Result<Void> add(@RequestBody EnterpriseDTO dto) {
        enterpriseService.addEnterprise(dto);
        return Result.success();
    }

    /** 管理员修改企业 */
    @OperationLog("修改企业")
    @PutMapping
    public Result<Void> update(@RequestBody EnterpriseDTO dto) {
        enterpriseService.updateEnterprise(dto);
        return Result.success();
    }

    /** 管理员删除企业 */
    @OperationLog("删除企业")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        enterpriseService.deleteEnterprise(id);
        return Result.success();
    }

    /** 获取当前用户的企业信息 */
    @GetMapping("/my")
    public Result<EnterpriseVO> my() {
        return Result.success(enterpriseService.getMyEnterprise());
    }

    /** 修改当前企业信息 */
    @PutMapping("/my")
    public Result<Void> updateMy(@RequestBody EnterpriseDTO dto) {
        enterpriseService.updateMyEnterprise(dto);
        return Result.success();
    }

    /** 启用/停用企业账号 */
    @OperationLog("企业状态变更")
    @PutMapping("/status")
    public Result<Void> changeStatus(@RequestBody @Valid StatusDTO dto) {
        enterpriseService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }
}
