package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.StorageDTO;
import com.trace.entity.StorageRecord;
import com.trace.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 入库管理接口
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    /** 入库列表 */
    @GetMapping("/list")
    public Result<PageResult<StorageRecord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String status) {
        return Result.success(storageService.listRecords(pageNum, pageSize, batchId, status));
    }

    /** 新增入库 */
    @PostMapping
    public Result<Void> add(@RequestBody StorageDTO dto) {
        storageService.addStorage(dto);
        return Result.success();
    }

    /** 上架操作 */
    @PutMapping("/shelf")
    public Result<Void> shelf(@RequestParam Long id) {
        storageService.shelf(id);
        return Result.success();
    }
}
