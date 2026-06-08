package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.SalesDTO;
import com.trace.entity.SalesRecord;
import com.trace.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 销售管理接口
 */
@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    /** 销售列表 */
    @GetMapping("/list")
    public Result<PageResult<SalesRecord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long storageId) {
        return Result.success(salesService.listSales(pageNum, pageSize, storageId));
    }

    /** 新增销售记录 */
    @PostMapping
    public Result<Void> add(@RequestBody SalesDTO dto) {
        salesService.addSales(dto);
        return Result.success();
    }
}
