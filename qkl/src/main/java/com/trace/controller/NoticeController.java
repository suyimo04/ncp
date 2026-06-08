package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.NoticeDTO;
import com.trace.dto.StatusDTO;
import com.trace.entity.SysNotice;
import com.trace.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理接口
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /** 公告列表 */
    @GetMapping("/list")
    public Result<PageResult<SysNotice>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(noticeService.listNotices(pageNum, pageSize, status, keyword));
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    public Result<SysNotice> detail(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    /** 新增公告 */
    @PostMapping
    public Result<Void> add(@RequestBody NoticeDTO dto) {
        noticeService.addNotice(dto);
        return Result.success();
    }

    /** 修改公告 */
    @PutMapping
    public Result<Void> update(@RequestBody NoticeDTO dto) {
        noticeService.updateNotice(dto);
        return Result.success();
    }

    /** 删除公告 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }

    /** 发布/下线 */
    @PutMapping("/status")
    public Result<Void> changeStatus(@RequestBody @Valid StatusDTO dto) {
        noticeService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }
}
