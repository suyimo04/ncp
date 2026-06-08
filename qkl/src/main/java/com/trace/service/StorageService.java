package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.StorageDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.ProductionBatch;
import com.trace.entity.StorageRecord;
import com.trace.exception.BusinessException;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.ProductionBatchMapper;
import com.trace.mapper.StorageRecordMapper;
import com.trace.util.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 入库管理服务
 */
@Slf4j
@Service
public class StorageService {

    @Autowired
    private StorageRecordMapper storageRecordMapper;
    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /**
     * 分页查询入库记录
     */
    public PageResult<StorageRecord> listRecords(Integer pageNum, Integer pageSize,
                                                  Long batchId, String status) {
        Page<StorageRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StorageRecord> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(StorageRecord::getBatchId, batchId);
        }
        if (status != null) {
            wrapper.eq(StorageRecord::getStatus, status);
        }

        // 商家只看自己的入库
        String role = RequestContext.getCurrentRole();
        if ("MERCHANT".equals(role)) {
            Long userId = RequestContext.getCurrentUserId();
            EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
            );
            if (ent != null) {
                wrapper.eq(StorageRecord::getMerchantId, ent.getId());
            }
        }

        wrapper.orderByDesc(StorageRecord::getCreateTime);
        Page<StorageRecord> result = storageRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增入库记录
     */
    public void addStorage(StorageDTO dto) {
        ProductionBatch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) throw new BusinessException("批次不存在");

        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (ent == null) throw new BusinessException("请先完善企业信息");

        StorageRecord record = new StorageRecord();
        BeanUtil.copyProperties(dto, record);
        record.setMerchantId(ent.getId());
        if (record.getStorageTime() == null) {
            record.setStorageTime(LocalDateTime.now());
        }
        record.setStatus("STORED");
        storageRecordMapper.insert(record);

        // 更新批次状态
        batch.setStatus("STORED");
        batchMapper.updateById(batch);
        log.info("批次 {} 入库完成", batch.getBatchNo());
    }

    /**
     * 上架操作
     */
    public void shelf(Long id) {
        StorageRecord record = storageRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("入库记录不存在");
        record.setStatus("ON_SHELF");
        storageRecordMapper.updateById(record);

        // 同步批次状态
        ProductionBatch batch = batchMapper.selectById(record.getBatchId());
        if (batch != null) {
            batch.setStatus("ON_SHELF");
            batchMapper.updateById(batch);
        }
    }
}
