package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.LogisticsDTO;
import com.trace.dto.LogisticsStatusDTO;
import com.trace.dto.LogisticsTrackDTO;
import com.trace.entity.LogisticsRecord;
import com.trace.entity.LogisticsTrack;
import com.trace.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物流运输接口
 */
@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    /** 运输列表 */
    @GetMapping("/list")
    public Result<PageResult<LogisticsRecord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String status) {
        return Result.success(logisticsService.listRecords(pageNum, pageSize, batchId, status));
    }

    /** 新增运输记录 */
    @PostMapping
    public Result<Void> add(@RequestBody LogisticsDTO dto) {
        logisticsService.addRecord(dto);
        return Result.success();
    }

    /** 更新运输状态 */
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid LogisticsStatusDTO dto) {
        logisticsService.updateStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }

    /** 添加物流轨迹 */
    @PostMapping("/track")
    public Result<Void> addTrack(@RequestBody LogisticsTrackDTO dto) {
        logisticsService.addTrack(dto);
        return Result.success();
    }

    /** 轨迹列表 */
    @GetMapping("/track/{recordId}")
    public Result<List<LogisticsTrack>> tracks(@PathVariable Long recordId) {
        return Result.success(logisticsService.listTracks(recordId));
    }
}
