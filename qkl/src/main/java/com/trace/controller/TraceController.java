package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.entity.QueryRecord;
import com.trace.service.TraceService;
import com.trace.util.ExcelExportUtil;
import com.trace.vo.TraceVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * 溯源查询接口
 */
@RestController
@RequestMapping("/api/trace")
public class TraceController {

    @Autowired
    private TraceService traceService;

    /** 根据溯源码查询（允许匿名访问） */
    @GetMapping("/query")
    public Result<TraceVO> query(@RequestParam String traceCode) {
        return Result.success(traceService.queryByTraceCode(traceCode));
    }

    /** 查询记录列表 */
    @GetMapping("/records")
    public Result<PageResult<QueryRecord>> records(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String traceCode) {
        return Result.success(traceService.listQueryRecords(pageNum, pageSize, traceCode));
    }

    /** 导出查询记录 */
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String traceCode) throws IOException {
        PageResult<QueryRecord> pageResult = traceService.listQueryRecords(1, 10000, traceCode);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (QueryRecord r : pageResult.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("traceCode", r.getTraceCode());
            row.put("batchNo", r.getBatchNo());
            row.put("queryTime", r.getQueryTime() != null ? r.getQueryTime().toString() : "");
            row.put("queryIp", r.getQueryIp());
            row.put("userId", r.getUserId() != null ? r.getUserId().toString() : "匿名");
            dataList.add(row);
        }
        List<String> headers = Arrays.asList("溯源码", "批次号", "查询时间", "查询IP", "用户ID");
        List<String> keys = Arrays.asList("traceCode", "batchNo", "queryTime", "queryIp", "userId");
        ExcelExportUtil.export(response, "查询记录", headers, keys, dataList);
    }
}
