package com.trace.controller;

import com.trace.common.Result;
import com.trace.service.StatsService;
import com.trace.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计分析接口
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /** 首页统计 */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(statsService.getDashboard());
    }

    /** 企业统计 */
    @GetMapping("/enterprise")
    public Result<Map<String, Object>> enterprise() {
        return Result.success(statsService.getEnterpriseStats());
    }

    /** 批次统计 */
    @GetMapping("/batch")
    public Result<Map<String, Object>> batch() {
        return Result.success(statsService.getBatchStats());
    }

    /** 上链统计 */
    @GetMapping("/blockchain")
    public Result<Map<String, Object>> blockchain() {
        return Result.success(statsService.getBlockchainStats());
    }

    /** 查询统计 */
    @GetMapping("/query")
    public Result<Map<String, Object>> query() {
        return Result.success(statsService.getQueryStats());
    }
}
