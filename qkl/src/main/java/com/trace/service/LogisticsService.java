package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.LogisticsDTO;
import com.trace.dto.LogisticsTrackDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.LogisticsRecord;
import com.trace.entity.LogisticsTrack;
import com.trace.entity.ProductionBatch;
import com.trace.exception.BusinessException;
import com.trace.mapper.*;
import com.trace.util.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流运输服务
 */
@Slf4j
@Service
public class LogisticsService {

    @Autowired
    private LogisticsRecordMapper logisticsRecordMapper;
    @Autowired
    private LogisticsTrackMapper trackMapper;
    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /**
     * 分页查询运输记录
     */
    public PageResult<LogisticsRecord> listRecords(Integer pageNum, Integer pageSize,
                                                    Long batchId, String status) {
        Page<LogisticsRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LogisticsRecord> wrapper = new LambdaQueryWrapper<>();

        if (batchId != null) {
            wrapper.eq(LogisticsRecord::getBatchId, batchId);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(LogisticsRecord::getStatus, status);
        }

        // 物流企业只能看自己的任务
        String role = RequestContext.getCurrentRole();
        if ("LOGISTICS".equals(role)) {
            Long userId = RequestContext.getCurrentUserId();
            EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
            );
            if (ent != null) {
                wrapper.eq(LogisticsRecord::getLogisticsId, ent.getId());
            }
        }

        wrapper.orderByDesc(LogisticsRecord::getCreateTime);
        Page<LogisticsRecord> result = logisticsRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增运输记录
     */
    public void addRecord(LogisticsDTO dto) {
        ProductionBatch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) throw new BusinessException("批次不存在");

        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (ent == null) throw new BusinessException("请先完善企业信息");

        LogisticsRecord record = new LogisticsRecord();
        BeanUtil.copyProperties(dto, record);
        record.setLogisticsId(ent.getId());
        record.setStatus("PENDING");
        logisticsRecordMapper.insert(record);

        // 更新批次状态
        batch.setStatus("SHIPPED");
        batchMapper.updateById(batch);

        log.info("新增运输记录，批次: {}", batch.getBatchNo());
    }

    /**
     * 更新运输状态
     */
    public void updateStatus(Long id, String status) {
        LogisticsRecord record = logisticsRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("运输记录不存在");

        record.setStatus(status);
        if ("IN_TRANSIT".equals(status) && record.getShipTime() == null) {
            record.setShipTime(LocalDateTime.now());
        }
        if ("ARRIVED".equals(status) || "SIGNED".equals(status)) {
            record.setArriveTime(LocalDateTime.now());
        }
        logisticsRecordMapper.updateById(record);

        // 同步更新批次状态
        ProductionBatch batch = batchMapper.selectById(record.getBatchId());
        if (batch != null) {
            if ("IN_TRANSIT".equals(status)) {
                batch.setStatus("IN_TRANSIT");
            }
            batchMapper.updateById(batch);
        }
    }

    /**
     * 添加物流轨迹
     */
    public void addTrack(LogisticsTrackDTO dto) {
        LogisticsRecord record = logisticsRecordMapper.selectById(dto.getLogisticsRecordId());
        if (record == null) throw new BusinessException("运输记录不存在");

        LogisticsTrack track = new LogisticsTrack();
        BeanUtil.copyProperties(dto, track);
        if (track.getTrackTime() == null) {
            track.setTrackTime(LocalDateTime.now());
        }
        trackMapper.insert(track);
    }

    /**
     * 获取运输记录的所有轨迹
     */
    public List<LogisticsTrack> listTracks(Long recordId) {
        return trackMapper.selectList(
                new LambdaQueryWrapper<LogisticsTrack>()
                        .eq(LogisticsTrack::getLogisticsRecordId, recordId)
                        .orderByAsc(LogisticsTrack::getTrackTime)
        );
    }
}
