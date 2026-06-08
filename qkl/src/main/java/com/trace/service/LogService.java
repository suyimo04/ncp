package com.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.entity.SysLog;
import com.trace.mapper.SysLogMapper;
import com.trace.util.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;

/**
 * 操作日志服务
 */
@Slf4j
@Service
public class LogService {

    @Autowired
    private SysLogMapper logMapper;

    /**
     * 异步记录操作日志
     */
    @Async
    public void recordLog(String operation, String method, String params, int result, String errorMsg) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setUserId(RequestContext.getCurrentUserId());
            sysLog.setUsername(RequestContext.getCurrentUsername());
            sysLog.setOperation(operation);
            sysLog.setMethod(method);
            sysLog.setParams(params);
            sysLog.setIp(RequestContext.getClientIp());
            sysLog.setResult(result);
            sysLog.setErrorMsg(errorMsg);
            logMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询日志
     */
    public PageResult<SysLog> listLogs(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysLog::getOperation, keyword)
                    .or().like(SysLog::getUsername, keyword);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        Page<SysLog> result = logMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }
}
