package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.SalesDTO;
import com.trace.entity.SalesRecord;
import com.trace.entity.StorageRecord;
import com.trace.exception.BusinessException;
import com.trace.mapper.SalesRecordMapper;
import com.trace.mapper.StorageRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 销售管理服务
 */
@Slf4j
@Service
public class SalesService {

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Autowired
    private StorageRecordMapper storageRecordMapper;

    /**
     * 分页查询销售记录
     */
    public PageResult<SalesRecord> listSales(Integer pageNum, Integer pageSize, Long storageId) {
        Page<SalesRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesRecord> wrapper = new LambdaQueryWrapper<>();
        if (storageId != null) {
            wrapper.eq(SalesRecord::getStorageId, storageId);
        }
        wrapper.orderByDesc(SalesRecord::getCreateTime);
        Page<SalesRecord> result = salesRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增销售记录
     */
    public void addSales(SalesDTO dto) {
        StorageRecord storage = storageRecordMapper.selectById(dto.getStorageId());
        if (storage == null) throw new BusinessException("入库记录不存在");

        SalesRecord record = new SalesRecord();
        BeanUtil.copyProperties(dto, record);
        if (record.getSalesTime() == null) {
            record.setSalesTime(LocalDateTime.now());
        }
        salesRecordMapper.insert(record);
        log.info("新增销售记录，入库ID: {}, 数量: {}", dto.getStorageId(), dto.getQuantity());
    }
}
