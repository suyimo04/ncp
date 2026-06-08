package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.QualityTestDTO;
import com.trace.entity.QualityTest;
import com.trace.service.QualityTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 质检管理接口
 */
@RestController
@RequestMapping("/api/test")
public class QualityTestController {

    @Autowired
    private QualityTestService testService;

    /** 质检列表 */
    @GetMapping("/list")
    public Result<PageResult<QualityTest>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long batchId) {
        return Result.success(testService.listTests(pageNum, pageSize, batchId));
    }

    /** 新增质检记录 */
    @PostMapping
    public Result<Void> add(@RequestBody QualityTestDTO dto) {
        testService.addTest(dto);
        return Result.success();
    }

    /** 质检详情 */
    @GetMapping("/{id}")
    public Result<QualityTest> detail(@PathVariable Long id) {
        return Result.success(testService.getById(id));
    }
}
