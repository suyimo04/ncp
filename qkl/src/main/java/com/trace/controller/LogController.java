package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.entity.SysLog;
import com.trace.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志接口
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    @Autowired
    private LogService logService;

    /** 日志列表 */
    @GetMapping("/list")
    public Result<PageResult<SysLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(logService.listLogs(pageNum, pageSize, keyword));
    }
}
